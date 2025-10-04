package kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity;

import static java.util.Objects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.policy.DiscountAmountCalculable;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums.CouponDiscountType;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums.UserCouponStatus;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "user_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class UserCouponEntity extends BaseTimeEntity implements DiscountAmountCalculable {
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

	public static UserCouponEntity of(Long userId, CouponEntity coupon, LocalDateTime now) {
		UserCouponEntity userCoupon = new UserCouponEntity();
		userCoupon.userId = userId;
		userCoupon.couponId = coupon.getId();
		userCoupon.name = coupon.getName();
		userCoupon.discountType = coupon.getDiscountType();
		userCoupon.discountAmount = coupon.getDiscountAmount();
		userCoupon.expiredAt = coupon.getExpiredAt();
		userCoupon.status = UserCouponStatus.AVAILABLE;
		userCoupon.issuedAt = now;
		userCoupon.lastUsedAt = null;
		userCoupon.lastCancelledAt = null;
		userCoupon.orderId = null;
		return userCoupon;
	}

	public void use(Long userId, LocalDateTime now, Long orderId) {
		authorize(userId);
		if (this.status.isUsed()) {
			throw new CommerceException(CommerceCode.UNAVAILABLE_USER_COUPON);
		}

		if (nonNull(this.expiredAt) && (expiredAt.isEqual(now) || expiredAt.isBefore(now))) {
			throw new CommerceException(CommerceCode.EXPIRED_COUPON);
		}

		this.status = UserCouponStatus.USED;
		this.lastUsedAt = now;
		this.orderId = orderId;
	}

	private void authorize(Long userId) {
		if (!this.userId.equals(userId)) {
			throw new CommerceException(CommerceCode.UNAUTHORIZED_USER);
		}
	}

	public BigDecimal calculateFinalAmount(BigDecimal originalAmount) {
		if (this.discountType.equals(CouponDiscountType.FIXED)) {
			return originalAmount.subtract(discountAmount);
		}

		BigDecimal discountRateAsDecimal = discountAmount.multiply(BigDecimal.valueOf(0.01)); // 할인율
		BigDecimal discountAmount = originalAmount.multiply(discountRateAsDecimal); // 할인 금액
		return originalAmount.subtract(discountAmount);
	}

	@Override
	public BigDecimal calculateDiscountAmount(BigDecimal originalAmount) {
		if (this.discountType.equals(CouponDiscountType.FIXED)) {
			return discountAmount;
		}

		BigDecimal discountRateAsDecimal = discountAmount.multiply(BigDecimal.valueOf(0.01)); // 할인율
		return originalAmount.multiply(discountRateAsDecimal); // 할인 금액
	}
}
