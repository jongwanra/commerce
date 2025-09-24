package kr.hhplus.be.commerce.presentation.api.cash.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;

public record CashChargeRequest(
	@NotNull(message = "충전 금액은 필수입니다.")
	@Parameter(description = "충전할 금액", example = "10000", required = true)
	BigDecimal amount
) {
}
