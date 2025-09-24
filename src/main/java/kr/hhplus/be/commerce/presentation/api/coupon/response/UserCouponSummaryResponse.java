package kr.hhplus.be.commerce.presentation.api.coupon.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums.CouponDiscountType;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums.UserCouponStatus;

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
