package kr.hhplus.be.commerce.application.coupon;

import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.coupon.model.Coupon;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;

public interface UserCouponIssueProcessor {
	Output execute(Command command);

	record Command(
		Long userId,
		Long couponId,
		LocalDateTime now
	) {
	}

	record Output(
		Coupon coupon,
		UserCoupon userCoupon
	) {
	}
}
