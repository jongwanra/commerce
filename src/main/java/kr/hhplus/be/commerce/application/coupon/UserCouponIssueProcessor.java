package kr.hhplus.be.commerce.application.coupon;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
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

		try {
			return new Output(
				couponJpaRepository.save(coupon),
				userCouponRepository.save(UserCouponEntity.of(command.userId, coupon, command.now))
			);
		} catch (DataIntegrityViolationException e) {
			// userId와 couponId의 unique 제약 조건 위반 시 중복 발급으로 간주합니다.
			// 위에서 coupon에 대한 Pessimistic Lock을 획득했기 때문에, 동시성 이슈로 인한 중복 발급은 발생하지 않습니다.
			// 발생하지 않더라도 혹시 모를 상황에 대비한 방어 코드입니다.
			log.warn("Failed to issue coupon due to DataIntegrityViolationException, message={}", e.getMessage());
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
