package kr.hhplus.be.commerce.coupon.presentation.controller.api;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kr.hhplus.be.commerce.coupon.presentation.response.UserCouponSummaryResponse;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.global.request.CursorPaginationRequest;
import kr.hhplus.be.commerce.global.response.CursorPagination;

public interface UserCouponReadPageApi {

	@Operation(
		tags = "UserCoupon",
		summary = "내 쿠폰 목록 페이지네이션 조회"
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
	})
	CursorPagination<UserCouponSummaryResponse> readPage(
		@Parameter(hidden = true) Long userId,
		@ParameterObject CursorPaginationRequest cursorPaginationRequest
	);
}
