package kr.hhplus.be.commerce.infrastructure.persistence.coupon;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.CouponEntity;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM coupon c WHERE c.id = :couponId")
	Optional<CouponEntity> findByIdWithLock(@Param("couponId") Long couponId);
}
