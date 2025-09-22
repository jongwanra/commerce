package kr.hhplus.be.commerce.product.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.product.domain.model.ProductDetailView;

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
