package kr.hhplus.be.commerce.infrastructure.persistence.coupon;

import static kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.QUserCouponEntity.*;
import static kr.hhplus.be.commerce.presentation.global.utils.Validator.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.UserCouponEntity;

public class UserCouponRepositoryImpl implements UserCouponRepository {
	private final UserCouponJpaRepository userCouponJpaRepository;
	private final JPAQueryFactory queryFactory;

	public UserCouponRepositoryImpl(UserCouponJpaRepository userCouponJpaRepository, EntityManager entityManager) {
		this.userCouponJpaRepository = userCouponJpaRepository;
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	@Override
	public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
		requireNonNull(List.of(
			Param.of(userId),
			Param.of(couponId)
		));
		return userCouponJpaRepository.existsByUserIdAndCouponId(userId, couponId);
	}

	@Override
	public UserCoupon save(UserCoupon userCoupon) {
		return userCouponJpaRepository.save(UserCouponEntity.fromDomain(userCoupon))
			.toDomain();
	}

	@Override
	public Page<UserCoupon> findPageByUserId(Long userId, Long lastId, Pageable pageable) {
		requireNonNull(List.of(
			Param.of(userId),
			Param.of(lastId),
			Param.of(pageable)
		));

		List<UserCouponEntity> userCoupons = queryFactory
			.selectFrom(userCouponEntity)
			.where(
				userCouponEntity.userId.eq(userId),
				userCouponEntity.id.lt(lastId)
			)
			.orderBy(userCouponEntity.id.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		final boolean hasNext = userCoupons.size() > pageable.getPageSize();
		if (hasNext) {
			userCoupons.remove(pageable.getPageSize());
		}

		final long totalCount = queryFactory
			.select(userCouponEntity.id.count())
			.from(userCouponEntity)
			.where(userCouponEntity.userId.eq(userId))
			.fetchFirst();

		return new PageImpl<>(userCoupons.stream().map(UserCouponEntity::toDomain).toList(), pageable, totalCount);
	}

	@Override
	public Optional<UserCoupon> findById(Long userCouponId) {
		return userCouponJpaRepository.findById(userCouponId)
			.map(UserCouponEntity::toDomain);
	}

}
