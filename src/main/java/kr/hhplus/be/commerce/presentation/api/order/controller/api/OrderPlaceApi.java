package kr.hhplus.be.commerce.presentation.api.order.controller.api;

import static kr.hhplus.be.commerce.presentation.global.utils.CommerceHttpRequestHeaderName.*;

import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.presentation.api.order.request.OrderPlaceRequest;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.presentation.global.response.EmptyResponse;

public interface OrderPlaceApi {
	@Operation(
		tags = "Order",
		summary = "주문 & 결제",
		description = """
			
			"""
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_USER),
		@ApiResponseErrorCode(CommerceCode.IDEMPOTENCY_KEY_IS_REQUIRED),
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
		@ApiResponseErrorCode(CommerceCode.INSUFFICIENT_CASH),
		@ApiResponseErrorCode(CommerceCode.MISMATCHED_EXPECTED_AMOUNT),
		@ApiResponseErrorCode(CommerceCode.ORDER_LINE_COMMANDS_IS_EMPTY),
		@ApiResponseErrorCode(CommerceCode.ORDER_QUANTITY_MUST_BE_POSITIVE),
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_PRODUCT),
		@ApiResponseErrorCode(CommerceCode.INSUFFICIENT_PRODUCT_STOCK),
		@ApiResponseErrorCode(CommerceCode.UNAVAILABLE_USER_COUPON)
	})
	EmptyResponse placeOrder(
		@Parameter(hidden = true) Long userId,
		@RequestHeader(name = X_COMMERCE_IDEMPOTENCY_KEY) String idempotencyKey,
		@RequestBody(required = true) OrderPlaceRequest request
	);
}
