package kr.hhplus.be.commerce.infrastructure.persistence.coupon;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.CouponEntity;

public class CouponRepositoryImpl implements CouponRepository {
	private final CouponJpaRepository couponJpaRepository;

	public CouponRepositoryImpl(CouponJpaRepository couponJpaRepository) {
		this.couponJpaRepository = couponJpaRepository;
	}

	@Override
	public Optional<Coupon> findByIdForUpdate(Long id) {
		return couponJpaRepository.findByIdForUpdate(id)
			.map(CouponEntity::toDomain);
	}

	@Override
	public Coupon save(Coupon coupon) {
		return couponJpaRepository.save(CouponEntity.fromDomain(coupon))
			.toDomain();
	}

	@Override
	public Optional<Coupon> findById(Long id) {
		return couponJpaRepository.findById(id)
			.map(CouponEntity::toDomain);
	}
}
