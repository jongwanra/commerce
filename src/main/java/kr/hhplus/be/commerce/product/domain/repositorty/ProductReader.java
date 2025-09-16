package kr.hhplus.be.commerce.product.domain.repositorty;

import java.util.Optional;

import kr.hhplus.be.commerce.global.response.CursorPage;
import kr.hhplus.be.commerce.product.domain.model.ProductDetailView;
import kr.hhplus.be.commerce.product.domain.model.ProductSummaryView;
import kr.hhplus.be.commerce.product.domain.repositorty.input.ProductReadPageInput;

public interface ProductReader {
	CursorPage<ProductSummaryView> readPage(ProductReadPageInput input);

	Optional<ProductDetailView> readById(Long productId);
}
