package kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums.CouponDiscountType;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Integer stock;

	LocalDateTime expiredAt;

	@Enumerated(EnumType.STRING)
	private CouponDiscountType discountType;
	private BigDecimal discountAmount;

	@Builder
	private CouponEntity(String name, Integer stock, LocalDateTime expiredAt, CouponDiscountType discountType,
		BigDecimal discountAmount) {
		this.name = name;
		this.stock = stock;
		this.expiredAt = expiredAt;
		this.discountType = discountType;
		this.discountAmount = discountAmount;
	}

	public void issue(LocalDateTime now) {
		validateIssuable(now);
		this.stock -= 1;
	}

	private void validateIssuable(LocalDateTime now) {
		validateExpired(now);
		validateStockIsRemaining();
	}

	private void validateStockIsRemaining() {
		if (this.stock <= 0) {
			throw new CommerceException(CommerceCode.OUT_OF_STOCK_COUPON);
		}
	}

	// 경계값 검증: 만료 시간과 현재 시간이 같은 경우도 만료된 것으로 간주 합니다.
	private void validateExpired(LocalDateTime now) {
		if (this.expiredAt.isBefore(now) || this.expiredAt.isEqual(now)) {
			throw new CommerceException(CommerceCode.EXPIRED_COUPON);
		}
	}
}
