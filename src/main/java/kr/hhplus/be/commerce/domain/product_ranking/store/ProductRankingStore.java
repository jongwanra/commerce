package kr.hhplus.be.commerce.domain.product_ranking.store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRankingView;

public interface ProductRankingStore {
	
	void increment(Long productId, Integer salesCount, LocalDate rankingDate, LocalDateTime now);

	List<ProductRankingView> readProductIdsDailyTopSelling(LocalDate rankingDate, int limit);

	List<ProductRankingView> readProductIdsDailyTopSelling(LocalDate today);

	void bulkInsert(List<ProductRanking> productRankings, LocalDate rankingDate, LocalDateTime now);

}
