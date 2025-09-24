package kr.hhplus.be.commerce.presentation.api.coupon.controller.api;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.presentation.api.coupon.response.UserCouponSummaryResponse;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.presentation.global.request.CursorPaginationRequest;
import kr.hhplus.be.commerce.presentation.global.response.CursorPage;

public interface UserCouponReadPageApi {

	@Operation(
		tags = "UserCoupon",
		summary = "내 쿠폰 목록 페이지네이션 조회"
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
	})
	CursorPage<UserCouponSummaryResponse> readPage(
		@Parameter(hidden = true) Long userId,
		@ParameterObject CursorPaginationRequest cursorPaginationRequest
	);
}
