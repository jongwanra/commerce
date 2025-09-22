package kr.hhplus.be.commerce.order.presentation.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.global.response.EmptyResponse;
import kr.hhplus.be.commerce.order.presentation.request.OrderPlaceRequest;

public interface OrderPlaceApi {
	@Operation(
		tags = "Order",
		summary = "주문"
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
		@ApiResponseErrorCode(CommerceCode.ORDER_LINE_COMMANDS_IS_EMPTY),
		@ApiResponseErrorCode(CommerceCode.ORDER_QUANTITY_MUST_BE_POSITIVE),
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_PRODUCT),
		@ApiResponseErrorCode(CommerceCode.INSUFFICIENT_PRODUCT_STOCK),
	})
	EmptyResponse placeOrder(
		@Parameter(hidden = true) Long userId,
		@RequestBody(required = true) OrderPlaceRequest request
	);
}
