package kr.hhplus.be.commerce.application.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.product.model.ProductSummaryView;
import kr.hhplus.be.commerce.domain.product.repository.ProductReader;
import kr.hhplus.be.commerce.domain.product.repository.input.ProductReadPageInput;
import kr.hhplus.be.commerce.domain.product.repository.input.enums.ProductSortType;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductSummaryResponse;
import kr.hhplus.be.commerce.presentation.global.response.CursorPage;
import lombok.RequiredArgsConstructor;

// TODO: Redis를 활용한 캐싱 전략을 통해서 조회 성능 개선 예정
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
