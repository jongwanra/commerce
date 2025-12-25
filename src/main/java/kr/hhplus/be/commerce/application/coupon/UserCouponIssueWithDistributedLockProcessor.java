package kr.hhplus.be.commerce.application.coupon;

import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserCouponIssueWithDistributedLockProcessor implements UserCouponIssueProcessor {
	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;

	/**
	 * 분산락을 활용한 사용자 쿠폰 발급 프로세서입니다.
	 *
	 * [분산락 적용 이유]
	 * 사용자 쿠폰 발급 비즈니스 로직은 단일 트랜잭션으로 원자성을 보장할 수 있기 때문에 Database lock만으로 충분합니다.
	 * 하지만 처리량이 많아질 수록, 락을 대기하는 Database connection이 많아집니다.
	 * 이는 Database에 부하를 줄 수 있습니다.
	 *
	 * [key값 선정 이유]
	 * 여러 사용자가 특정 쿠폰에 대해 발급을 시도하는 비즈니스 로직입니다.
	 * 공유 자원이 특정 쿠폰이기 때문에 특정 쿠폰에 락을 거는 것만으로 동시성 제어하기 충분합니다.
	 *
	 * [Redis가 다운될 경우]
	 * 진입 중, coupon record에 비관적 잠금이 걸려있기 때문에 동시성 제어함에 있어 안전합니다.
	 */
	@DistributedLock(key = "coupon", keyExpression = "#command.couponId()", waitTime = 30, leaseTime = 27)
	@Transactional
	public Output execute(Command command) {
		Coupon issuedCoupon = couponRepository.findById(command.couponId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_COUPON))
			.issue(command.now());

		if (userCouponRepository.existsByUserIdAndCouponId(command.userId(), command.couponId())) {
			throw new CommerceException(CommerceCode.ALREADY_ISSUED_COUPON);
		}

		return new Output(
			couponRepository.save(issuedCoupon),
			userCouponRepository.save(UserCoupon.of(command.userId(), issuedCoupon, command.now()))
		);
	}

}
