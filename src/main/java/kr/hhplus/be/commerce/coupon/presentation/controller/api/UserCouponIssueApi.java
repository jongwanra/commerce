package kr.hhplus.be.commerce.coupon.presentation.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.global.response.EmptyResponse;

public interface UserCouponIssueApi {

	@Operation(
		tags = "UserCoupon",
		summary = "쿠폰 발급"
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_COUPON),
		@ApiResponseErrorCode(CommerceCode.EXPIRED_COUPON),
		@ApiResponseErrorCode(CommerceCode.ALREADY_ISSUED_COUPON),
		@ApiResponseErrorCode(CommerceCode.OUT_OF_STOCK_COUPON)
	})
	EmptyResponse issue(@Parameter(hidden = true) Long userId,
		@Parameter(required = true, description = "쿠폰 고유 식별자") Long couponId);
}
