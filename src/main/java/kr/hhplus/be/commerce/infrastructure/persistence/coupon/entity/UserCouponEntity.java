package kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.coupon.model.enums.CouponDiscountType;
import kr.hhplus.be.commerce.domain.coupon.model.enums.UserCouponStatus;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "user_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UserCouponEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long couponId;
	private Long orderId;
	@Column(name = "name_snapshot", nullable = false)
	private String name;

	@Column(name = "discount_type_snapshot", updatable = false)
	@Enumerated(EnumType.STRING)
	private CouponDiscountType discountType;
	@Column(name = "discount_amount_snapshot", updatable = false)
	private BigDecimal discountAmount;
	@Column(name = "expired_at_snapshot", updatable = false)
	private LocalDateTime expiredAt;

	@Enumerated(EnumType.STRING)
	private UserCouponStatus status;

	private LocalDateTime issuedAt;
	private LocalDateTime lastUsedAt;
	private LocalDateTime lastCancelledAt;

	public static UserCouponEntity fromDomain(UserCoupon userCoupon) {
		return UserCouponEntity.builder()
			.id(userCoupon.id())
			.userId(userCoupon.userId())
			.couponId(userCoupon.couponId())
			.orderId(userCoupon.orderId())
			.name(userCoupon.name())
			.discountType(userCoupon.discountType())
			.discountAmount(userCoupon.discountAmount())
			.status(userCoupon.status())
			.issuedAt(userCoupon.issuedAt())
			.lastUsedAt(userCoupon.lastUsedAt())
			.expiredAt(userCoupon.expiredAt())
			.lastCancelledAt(userCoupon.lastCancelledAt())
			.build();
	}

	public UserCoupon toDomain() {
		return UserCoupon.restore(
			id,
			userId,
			couponId,
			orderId,
			name,
			discountType,
			discountAmount,
			status,
			issuedAt,
			expiredAt,
			lastUsedAt,
			lastCancelledAt
		);
	}
}
