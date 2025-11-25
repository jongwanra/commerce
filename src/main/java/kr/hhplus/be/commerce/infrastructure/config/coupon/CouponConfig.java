package kr.hhplus.be.commerce.infrastructure.config.coupon;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.commerce.application.coupon.UserCouponIssueProcessor;
import kr.hhplus.be.commerce.application.coupon.UserCouponIssueWithDistributedLockProcessor;
import kr.hhplus.be.commerce.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.CouponJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.CouponRepositoryImpl;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponRepositoryImpl;

@Configuration
public class CouponConfig {
	@Bean
	CouponRepository couponRepository(CouponJpaRepository couponJpaRepository) {
		return new CouponRepositoryImpl(couponJpaRepository);
	}

	@Bean
	public UserCouponRepository userCouponRepository(UserCouponJpaRepository userCouponJpaRepository,
		EntityManager entityManager) {
		return new UserCouponRepositoryImpl(userCouponJpaRepository, entityManager);
	}

	@Bean
	public UserCouponIssueProcessor userCouponIssueProcessor(CouponRepository couponRepository,
		UserCouponRepository userCouponRepository) {
		return new UserCouponIssueWithDistributedLockProcessor(couponRepository, userCouponRepository);
	}

}
