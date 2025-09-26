package kr.hhplus.be.commerce.presentation.api.payment.controller.api;

import static kr.hhplus.be.commerce.presentation.global.utils.CommerceHttpRequestHeaderName.*;

import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.presentation.api.payment.request.PaymentMakeRequest;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.presentation.global.response.EmptyResponse;

public interface PaymentMakeApi {
	@Operation(
		tags = "Payment",
		summary = "결제",
		description = """
			사용자의 잔액으로 주문한 상품을 결제합니다.
			"""
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.IDEMPOTENCY_KEY_IS_REQUIRED),
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
		@ApiResponseErrorCode(CommerceCode.UNAUTHORIZED_USER),
		@ApiResponseErrorCode(CommerceCode.INSUFFICIENT_CASH),
		@ApiResponseErrorCode(CommerceCode.MISMATCHED_EXPECTED_AMOUNT),
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_ORDER),
		@ApiResponseErrorCode(CommerceCode.UNAVAILABLE_USER_COUPON)
	})
	EmptyResponse makePayment(
		@Parameter(hidden = true) Long userId,
		@RequestHeader(name = X_COMMERCE_IDEMPOTENCY_KEY) String idempotencyKey,
		@RequestBody(required = true) PaymentMakeRequest request
	);
}
