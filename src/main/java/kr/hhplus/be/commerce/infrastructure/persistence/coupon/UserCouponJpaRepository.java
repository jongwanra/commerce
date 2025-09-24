package kr.hhplus.be.commerce.infrastructure.persistence.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.UserCouponEntity;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity, Long> {
	boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
