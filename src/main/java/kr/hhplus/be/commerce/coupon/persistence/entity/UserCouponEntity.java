package kr.hhplus.be.commerce.coupon.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.commerce.coupon.persistence.entity.enums.CouponDiscountType;
import kr.hhplus.be.commerce.coupon.persistence.entity.enums.UserCouponStatus;
import kr.hhplus.be.commerce.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "user_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class UserCouponEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long couponId;
	private Long orderId;
	private String originName;

	@Enumerated(EnumType.STRING)
	private CouponDiscountType originDiscountType;
	private BigDecimal originDiscountAmount;
	private LocalDateTime originExpiredAt;

	@Enumerated(EnumType.STRING)
	private UserCouponStatus status;

	private LocalDateTime issuedAt;
	private LocalDateTime lastUsedAt;
	private LocalDateTime lastCancelledAt;

	public static UserCouponEntity of(Long userId, CouponEntity coupon, LocalDateTime now) {
		UserCouponEntity userCoupon = new UserCouponEntity();
		userCoupon.userId = userId;
		userCoupon.couponId = coupon.getId();
		userCoupon.originName = coupon.getName();
		userCoupon.originDiscountType = coupon.getDiscountType();
		userCoupon.originDiscountAmount = coupon.getDiscountAmount();
		userCoupon.originExpiredAt = coupon.getExpiredAt();
		userCoupon.status = UserCouponStatus.AVAILABLE;
		userCoupon.issuedAt = now;
		userCoupon.lastUsedAt = null;
		userCoupon.lastCancelledAt = null;
		userCoupon.orderId = null;
		return userCoupon;
	}

}
