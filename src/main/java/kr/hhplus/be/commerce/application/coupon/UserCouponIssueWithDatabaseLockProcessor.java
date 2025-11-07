package kr.hhplus.be.commerce.application.coupon;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserCouponIssueWithDatabaseLockProcessor implements UserCouponIssueProcessor {
	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;

	@Override
	@Transactional
	public Output execute(Command command) {
		Coupon issuedCoupon = couponRepository.findByIdForUpdate(command.couponId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
			.issue(command.now());

		if (userCouponRepository.existsByUserIdAndCouponId(command.userId(), command.couponId())) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}

		try {
			return new Output(
				couponRepository.save(issuedCoupon),
				userCouponRepository.save(UserCoupon.of(command.userId(), issuedCoupon, command.now()))
			);
		} catch (DataIntegrityViolationException e) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}
	}

}
