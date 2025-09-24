package kr.hhplus.be.commerce.domain.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductSummaryView(
	Long id,
	String name,
	BigDecimal price,
	LocalDateTime createdAt
) {
}
