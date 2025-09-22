package kr.hhplus.be.commerce.product.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.product.domain.model.ProductSummaryView;

public record ProductSummaryResponse(
	Long id,
	String name,
	BigDecimal price,
	LocalDateTime createdAt
) {
	public static ProductSummaryResponse from(ProductSummaryView product) {
		return new ProductSummaryResponse(
			product.id(),
			product.name(),
			product.price(),
			product.createdAt()
		);
	}
}
