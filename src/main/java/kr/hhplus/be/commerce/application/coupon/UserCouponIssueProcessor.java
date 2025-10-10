package kr.hhplus.be.commerce.application.coupon;

import java.time.LocalDateTime;

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
public class UserCouponIssueProcessor {
	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;

	@Transactional
	public Output execute(Command command) {
		// coupon의 Pessimistic Lock을 획득하여 동시성 이슈를 제어합니다.
		Coupon coupon = couponRepository.findByIdWithLock(command.couponId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON));

		Coupon issuedCoupon = coupon.issue(command.now);

		if (userCouponRepository.existsByUserIdAndCouponId(command.userId, command.couponId)) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}

		return new Output(
			couponRepository.save(issuedCoupon),
			userCouponRepository.save(UserCoupon.of(command.userId, coupon, command.now))
		);
	}

	public record Command(
		Long userId,
		Long couponId,
		LocalDateTime now
	) {
	}

	public record Output(
		Coupon coupon,
		UserCoupon userCoupon
	) {
	}
}
