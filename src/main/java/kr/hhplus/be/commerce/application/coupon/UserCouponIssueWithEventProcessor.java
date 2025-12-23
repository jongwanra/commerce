package kr.hhplus.be.commerce.application.coupon;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.coupon.event.CouponIssuedEvent;
import kr.hhplus.be.commerce.domain.coupon.repository.CouponStore;
import kr.hhplus.be.commerce.domain.event.InternalEventPublisher;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.global.time.TimeProvider;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.result.CouponIssueResult;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCouponIssueWithEventProcessor {
	private final InternalEventPublisher internalEventPublisher;
	private final TimeProvider timeProvider;
	private final CouponStore couponStore;

	/**
	 * 기존의 잠금 방식(DB 잠금, 분산락 잠금)으로는, TPS 20이 최대인 걸로 부하 테스트를 하면서 알 수 있었습니다.
	 * 2,000명이 갑자기 몰린다는 상황을 가정했을 때, 시스템 장애가 명확합니다.
	 *
	 * 선착순 이벤트 쿠폰의 경우에는 사전에 이벤트 진행 10분 전에,
	 * Redis의 키값 추가가 필요합니다.
	 *
	 * SET coupon:1:stock 1000
	 * @see kr.hhplus.be.commerce.application.event.CouponIssuedEventListener
	 *
	 */

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void execute(Command command) {
		final LocalDateTime now = timeProvider.now();
		final CouponIssueResult result = couponStore.issue(command.couponId, command.userId);

		if (result == CouponIssueResult.DUPLICATE) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}
		if (result == CouponIssueResult.SOLD_OUT) {
			throw new CommerceException(CommerceCode.OUT_OF_STOCK_COUPON);
		}

		internalEventPublisher.publish(CouponIssuedEvent.of(command.couponId(), command.userId(), now));

	}

	public record Command(
		Long userId,
		Long couponId,
		LocalDateTime now
	) {
	}

}
