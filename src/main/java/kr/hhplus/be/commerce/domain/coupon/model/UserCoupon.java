package kr.hhplus.be.commerce.domain.coupon.model;

import static java.util.Objects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.coupon.model.enums.CouponDiscountType;
import kr.hhplus.be.commerce.domain.coupon.model.enums.UserCouponStatus;
import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.policy.DiscountAmountCalculable;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserCoupon(
	Long id,
	Long userId,
	Long couponId,
	Long orderId,
	String name,
	CouponDiscountType discountType,
	BigDecimal discountAmount,
	UserCouponStatus status,
	LocalDateTime expiredAt,
	LocalDateTime issuedAt,
	LocalDateTime lastUsedAt,
	LocalDateTime lastCancelledAt
) implements DiscountAmountCalculable {

	public static UserCoupon of(Long userId, Coupon coupon, LocalDateTime now) {
		return UserCoupon.builder()
			.userId(userId)
			.couponId(coupon.id())
			.name(coupon.name())
			.discountType(coupon.discountType())
			.discountAmount(coupon.discountAmount())
			.expiredAt(coupon.expiredAt())
			.status(UserCouponStatus.AVAILABLE)
			.issuedAt(now)
			.build();
	}

	public UserCoupon use(Long userId, LocalDateTime now, Long orderId) {
		authorize(userId);
		if (this.status.isUsed()) {
			throw new CommerceException(CommerceCode.UNAVAILABLE_USER_COUPON);
		}

		if (nonNull(this.expiredAt) && (expiredAt.isEqual(now) || expiredAt.isBefore(now))) {
			throw new CommerceException(CommerceCode.EXPIRED_COUPON);
		}
		return UserCoupon.builder()
			.id(id)
			.userId(userId)
			.orderId(orderId)
			.name(name)
			.discountType(discountType)
			.discountAmount(discountAmount)
			.expiredAt(expiredAt)
			.issuedAt(issuedAt)
			.lastCancelledAt(lastCancelledAt)
			.status(UserCouponStatus.USED)
			.lastUsedAt(now)
			.build();
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

	@InfrastructureOnly
	public static UserCoupon restore(Long id,
		Long userId,
		Long couponId,
		Long orderId,
		String name,
		CouponDiscountType discountType,
		BigDecimal discountAmount,
		UserCouponStatus status,
		LocalDateTime issuedAt,
		LocalDateTime expiredAt,
		LocalDateTime lastUsedAt,
		LocalDateTime lastCancelledAt) {
		return UserCoupon.builder()
			.id(id)
			.userId(userId)
			.couponId(couponId)
			.orderId(orderId)
			.name(name)
			.discountType(discountType)
			.discountAmount(discountAmount)
			.status(status)
			.expiredAt(expiredAt)
			.issuedAt(issuedAt)
			.lastUsedAt(lastUsedAt)
			.lastCancelledAt(lastCancelledAt)
			.build();
	}

}
