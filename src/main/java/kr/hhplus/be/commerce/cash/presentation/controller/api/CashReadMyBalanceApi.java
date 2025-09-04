package kr.hhplus.be.commerce.cash.presentation.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kr.hhplus.be.commerce.cash.presentation.response.CashDetailResponse;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;

public interface CashReadMyBalanceApi {
	@Operation(
		tags = "Cash",
		summary = "잔액 조회"
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.UNAUTHENTICATED_USER),
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_CASH),
	})
	CashDetailResponse readMyBalance(
		@Parameter(hidden = true) Long userId
	);
}
