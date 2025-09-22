package kr.hhplus.be.commerce.cash.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashDetailResponse(
	BigDecimal balance,
	LocalDateTime modifiedAt
) {
}
