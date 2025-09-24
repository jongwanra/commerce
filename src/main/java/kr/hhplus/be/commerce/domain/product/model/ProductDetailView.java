package kr.hhplus.be.commerce.domain.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailView(
	Long id,
	String name,
	BigDecimal price,
	Integer stock,
	LocalDateTime createdAt
) {
}
