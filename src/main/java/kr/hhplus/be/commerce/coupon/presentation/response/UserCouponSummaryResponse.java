package kr.hhplus.be.commerce.coupon.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.coupon.persistence.entity.enums.CouponDiscountType;
import kr.hhplus.be.commerce.coupon.persistence.entity.enums.UserCouponStatus;

public record UserCouponSummaryResponse(
	Long id,
	Long couponId,
	String name,
	CouponDiscountType discountType,
	BigDecimal discountValue,
	UserCouponStatus status,
	LocalDateTime issuedAt,
	LocalDateTime expiredAt
) {
}
