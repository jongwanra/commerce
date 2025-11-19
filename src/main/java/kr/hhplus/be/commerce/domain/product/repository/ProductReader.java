package kr.hhplus.be.commerce.domain.product.repository;

import java.util.List;
import java.util.Optional;

import kr.hhplus.be.commerce.domain.product.model.ProductDetailView;
import kr.hhplus.be.commerce.domain.product.model.ProductSummaryView;
import kr.hhplus.be.commerce.domain.product.repository.input.ProductReadPageInput;
import kr.hhplus.be.commerce.presentation.global.response.CursorPage;

public interface ProductReader {
	CursorPage<ProductSummaryView> readPage(ProductReadPageInput input);

	Optional<ProductDetailView> readById(Long productId);

	List<ProductSummaryView> readAllByIdIn(List<Long> productIds);
}
