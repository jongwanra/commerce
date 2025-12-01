package kr.hhplus.be.commerce.application.product;

import static java.util.stream.Collectors.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.product.model.ProductSummaryView;
import kr.hhplus.be.commerce.domain.product.repository.ProductReader;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRankingView;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingReader;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReadAllDailyTopSellingQueryManager {
	private final ProductReader productReader;
	private final ProductRankingStore productRankingStore;
	private final ProductRankingReader productRankingReader;

	@Transactional(readOnly = true)
	public List<ProductSummaryResponse> read(Query query) {

		List<Long> productIds = readProductIdsDailyTopSelling(query);

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

	private List<Long> readProductIdsDailyTopSelling(Query query) {
		try {
			return productRankingStore.readProductIdsDailyTopSelling(query.today, query.limit)
				.stream()
				.map(ProductRankingView::productId)
				.toList();
		} catch (Exception e) {
			// Redis 시스템에 문제가 발생할 경우 fallback으로 데이터베이스에서 데이터를 조회합니다.
			return productRankingReader.readProductIdsByRankingDateOrderBySalesCountDesc(query.today, query.limit);
		}
	}

	public record Query(
		LocalDate today,
		int limit
	) {
	}
}
