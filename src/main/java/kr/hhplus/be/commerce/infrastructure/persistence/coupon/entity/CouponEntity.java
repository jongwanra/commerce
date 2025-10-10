package kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.model.enums.CouponDiscountType;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
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

	public static CouponEntity fromDomain(Coupon coupon) {
		return CouponEntity.builder()
			.id(coupon.id())
			.name(coupon.name())
			.stock(coupon.stock())
			.expiredAt(coupon.expiredAt())
			.discountType(coupon.discountType())
			.discountAmount(coupon.discountAmount())
			.build();
	}

	public Coupon toDomain() {
		return Coupon.restore(
			id,
			name,
			stock,
			expiredAt,
			discountType,
			discountAmount
		);
	}
}
