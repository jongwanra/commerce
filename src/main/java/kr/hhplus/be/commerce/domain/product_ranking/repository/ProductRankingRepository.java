package kr.hhplus.be.commerce.domain.product_ranking.repository;

import java.time.LocalDate;
import java.util.List;

import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;

public interface ProductRankingRepository {

	List<ProductRanking> saveAll(List<ProductRanking> productRankings);

	List<ProductRanking> findAllByRankingDate(LocalDate rankingDate);
}
