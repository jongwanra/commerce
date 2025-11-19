package kr.hhplus.be.commerce.application.product;

import static java.util.stream.Collectors.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.product.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product.model.ProductSummaryView;
import kr.hhplus.be.commerce.domain.product.repository.ProductReader;
import kr.hhplus.be.commerce.domain.product.store.ProductRankingStore;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReadAllDailyTopSellingQueryManager {
	private final ProductReader productReader;
	private final ProductRankingStore productRankingStore;

	@Transactional(readOnly = true)
	public List<ProductSummaryResponse> read(Query query) {
		List<Long> productIds = productRankingStore.readAllByRankingDate(query.today, query.limit)
			.stream()
			.map(ProductRanking::productId)
			.toList();

		Map<Long, ProductSummaryView> productIdToProductMap = productReader.readAllByIdIn(productIds)
			.stream()
			.collect(toMap(ProductSummaryView::id, it -> it));
		
		return productIds
			.stream()
			.filter(productIdToProductMap::containsKey)
			.map(productIdToProductMap::get)
			.map(ProductSummaryResponse::from)
			.toList();
	}

	public record Query(
		LocalDate today,
		int limit
	) {
	}
}
