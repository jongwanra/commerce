package kr.hhplus.be.commerce.presentation.api.cash.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashDetailResponse(
	BigDecimal balance,
	LocalDateTime modifiedAt
) {
}
