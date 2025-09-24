package kr.hhplus.be.commerce.presentation.api.product.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.product.model.ProductDetailView;

public record ProductDetailResponse(
	Long id,
	String name,
	BigDecimal price,
	Integer stock,
	LocalDateTime createdAt
) {
	public static ProductDetailResponse from(ProductDetailView productDetailView) {
		return new ProductDetailResponse(
			productDetailView.id(),
			productDetailView.name(),
			productDetailView.price(),
			productDetailView.stock(),
			productDetailView.createdAt()
		);
	}
}
