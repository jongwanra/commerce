package kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponDiscountType {
	PERCENT("백분율로 할인"),
	FIXED("고정 금액으로 할인");

	private final String description;
}
