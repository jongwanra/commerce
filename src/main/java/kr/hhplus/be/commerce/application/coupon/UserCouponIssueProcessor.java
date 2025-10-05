package kr.hhplus.be.commerce.application.coupon;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.CouponJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.CouponEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.UserCouponEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCouponIssueProcessor {
	private final CouponJpaRepository couponJpaRepository;
	private final UserCouponRepository userCouponRepository;

	@Transactional
	public Output execute(Command command) {
		// coupon의 Pessimistic Lock을 획득하여 동시성 이슈를 제어합니다.
		CouponEntity coupon = couponJpaRepository.findByIdWithLock(command.couponId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON));
		coupon.issue(command.now);

		if (userCouponRepository.existsByUserIdAndCouponId(command.userId, command.couponId)) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}

		return new Output(
			couponJpaRepository.save(coupon),
			userCouponRepository.save(UserCouponEntity.of(command.userId, coupon, command.now))
		);
	}

	public record Command(
		Long userId,
		Long couponId,
		LocalDateTime now
	) {
	}

	public record Output(
		CouponEntity coupon,
		UserCouponEntity userCoupon
	) {
	}
}
