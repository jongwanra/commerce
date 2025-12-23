package kr.hhplus.be.commerce.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssuedEventListener {
	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;

	@Async
	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(CouponIssuedEvent event) {
		Coupon issuedCoupon = couponRepository.findByIdForUpdate(event.couponId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
			.issue(event.occurredAt());

		try {
			userCouponRepository.save(UserCoupon.of(event.userId(), issuedCoupon, event.occurredAt()));
			couponRepository.save(issuedCoupon);
		} catch (DataIntegrityViolationException e) {
			// ...
		}
	}
}
