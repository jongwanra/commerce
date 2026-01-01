package kr.hhplus.be.commerce.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.coupon.event.CouponIssuedEvent;
import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.model.message_payload.UserCouponIssuedMessagePayload;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssuedEventListener {
	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;
	private final MessageRepository messageRepository;

	@Async
	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Retryable(noRetryFor = {DataIntegrityViolationException.class}, retryFor = {
		DataAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	public void handle(CouponIssuedEvent event) {
		try {
			if (userCouponRepository.existsByUserIdAndCouponId(event.userId(), event.couponId())) {
				return;
			}
			
			Coupon issuedCoupon = couponRepository.findByIdForUpdate(event.couponId())
				.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
				.issue(event.occurredAt());

			userCouponRepository.save(UserCoupon.of(event.userId(), issuedCoupon, event.occurredAt()));
			couponRepository.save(issuedCoupon);
		} catch (DataIntegrityViolationException e) {
			log.warn("쿠폰 중복 발급 요청이 발생했습니다. userId={}, couponId={}", event.userId(), event.couponId());
			throw e;
		} catch (Exception e) {
			log.error("쿠폰 발급 이벤트 처리 중 알 수 없는 에러가 발생했습니다. userId={}, couponId={}", event.userId(), event.couponId(), e);
			throw e; // 재시도
		}
	}

	@Recover
	public void recover(Exception e, CouponIssuedEvent event) {
		log.error("모든 재시도 처리 요청이 실패했습니다. userId={}, couponId={}", event.userId(), event.couponId(), e);

		messageRepository.save(
			Message.ofDeadLetter(
				event.couponId(),
				MessageTargetType.COUPON,
				UserCouponIssuedMessagePayload.of(event.occurredAt(), event.userId(), event.couponId()),
				event.occurredAt(),
				e.getMessage()
			));
	}
}
