package kr.hhplus.be.commerce.payment.presentation.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.global.response.EmptyResponse;
import kr.hhplus.be.commerce.payment.presentation.request.PaymentMakeRequest;

public interface PaymentMakeApi {
	@Operation(
		tags = "Payment",
		summary = "결제",
		description = """
			사용자의 잔액으로 주문한 상품을 결제합니다.
			"""
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
		@ApiResponseErrorCode(CommerceCode.UNAUTHORIZED_USER),
		@ApiResponseErrorCode(CommerceCode.INSUFFICIENT_CASH),
		@ApiResponseErrorCode(CommerceCode.MISMATCHED_EXPECTED_AMOUNT),
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_ORDER),
	})
	EmptyResponse makePayment(
		@Parameter(hidden = true) Long userId,
		@RequestBody(required = true) PaymentMakeRequest request
	);
}
