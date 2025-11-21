package kr.hhplus.be.commerce.domain.product_ranking.repository;

import java.time.LocalDate;
import java.util.List;

public interface ProductRankingReader {
	List<Long> readProductIdsByRankingDateOrderBySalesCountDesc(LocalDate today, int limit);
}
