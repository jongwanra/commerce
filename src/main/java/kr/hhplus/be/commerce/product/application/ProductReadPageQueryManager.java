package kr.hhplus.be.commerce.product.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.global.response.CursorPage;
import kr.hhplus.be.commerce.product.domain.model.ProductSummaryView;
import kr.hhplus.be.commerce.product.domain.repositorty.ProductReader;
import kr.hhplus.be.commerce.product.domain.repositorty.input.ProductReadPageInput;
import kr.hhplus.be.commerce.product.domain.repositorty.input.enums.ProductSortType;
import kr.hhplus.be.commerce.product.presentation.response.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReadPageQueryManager {
	private final ProductReader productReader;

	@Transactional(readOnly = true)
	public CursorPage<ProductSummaryResponse> read(Query query) {
		CursorPage<ProductSummaryView> productPage = productReader.readPage(
			ProductReadPageInput.builder()
				.lastId(query.lastId)
				.size(query.size)
				.sortType(query.sortType)
				.build()
		);

		return CursorPage.of(
			productPage.hasNext(),
			productPage.totalCount(),
			productPage
				.items()
				.stream()
				.map(ProductSummaryResponse::from)
				.toList()
		);
	}

	public record Query(
		Long lastId,
		Integer size,
		ProductSortType sortType
	) {

	}
}
