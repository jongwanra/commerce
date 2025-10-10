package kr.hhplus.be.commerce.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;

/**
 * 커서 기반 페이지네이션을 Querydsl을 이용하여 구현하기 위해 UserCouponRepository 추상화 작업을 진행했습니다.
 */
public interface UserCouponRepository {
	boolean existsByUserIdAndCouponId(Long userId, Long couponId);

	UserCoupon save(UserCoupon userCoupon);

	Page<UserCoupon> findPageByUserId(Long userId, Long lastId, Pageable pageable);

	Optional<UserCoupon> findById(Long id);
}
