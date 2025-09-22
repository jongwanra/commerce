package kr.hhplus.be.commerce.cash.presentation.controller.api;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kr.hhplus.be.commerce.cash.presentation.response.CashChargeRequest;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.global.response.EmptyResponse;

public interface CashChargeApi {
	@Operation(
		tags = "Cash",
		summary = "잔액 충전"
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_CASH),
		@ApiResponseErrorCode(CommerceCode.CHARGE_AMOUNT_MUST_BE_POSITIVE),
		@ApiResponseErrorCode(CommerceCode.CHARGE_AMOUNT_PER_ONCE_EXCEEDS_LIMIT)
	})
	EmptyResponse charge(
		@Parameter(hidden = true) Long userId,
		@ParameterObject CashChargeRequest request
	);
}
