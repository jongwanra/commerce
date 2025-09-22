package kr.hhplus.be.commerce.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity, Long> {
	boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
