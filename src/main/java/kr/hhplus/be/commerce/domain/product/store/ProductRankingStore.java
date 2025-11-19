package kr.hhplus.be.commerce.domain.product.store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import kr.hhplus.be.commerce.domain.product.model.ProductRanking;

public interface ProductRankingStore {

	void increment(List<Long> productIds, LocalDate rankingDate, LocalDateTime now);

	List<ProductRanking> readAllByRankingDate(LocalDate rankingDate, int limit);
}
