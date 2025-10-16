package kr.hhplus.be.commerce.domain.coupon.repository;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.coupon.model.Coupon;

public interface CouponRepository {
	Optional<Coupon> findByIdForUpdate(Long id);

	Coupon save(Coupon coupon);

	Optional<Coupon> findById(Long id);
}
