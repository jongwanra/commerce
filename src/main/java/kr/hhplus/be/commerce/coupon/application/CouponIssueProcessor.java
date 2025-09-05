package kr.hhplus.be.commerce.coupon.application;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.coupon.persistence.CouponJpaRepository;
import kr.hhplus.be.commerce.coupon.persistence.UserCouponJpaRepository;
import kr.hhplus.be.commerce.coupon.persistence.entity.CouponEntity;
import kr.hhplus.be.commerce.coupon.persistence.entity.UserCouponEntity;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueProcessor {
	private final CouponJpaRepository couponJpaRepository;
	private final UserCouponJpaRepository userCouponJpaRepository;

	@Transactional
	public Output execute(Command command) {
		// coupon의 Pessimistic Lock을 획득하여 동시성 이슈를 제어합니다.
		CouponEntity coupon = couponJpaRepository.findByIdWithLock(command.couponId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON));
		coupon.issue(command.now);

		try {
			UserCouponEntity savedUserCoupon = userCouponJpaRepository.save(
				UserCouponEntity.of(command.userId, coupon, command.now));
			return new Output(coupon, savedUserCoupon);
		} catch (DataIntegrityViolationException e) {
			log.warn("이미 발급된 쿠폰입니다. userId: {}, couponId: {}", command.userId, command.couponId);
			// userId, couponId에 unique 제약조건이 걸려있으므로, 중복 발급 시도 시 예외가 발생합니다.
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}

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
