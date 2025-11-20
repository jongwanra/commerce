package kr.hhplus.be.commerce.infrastructure.persistence.product_ranking;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRankingRepositoryImpl implements ProductRankingRepository {
	private final ProductRankingJpaRepository productRankingJpaRepository;
	
	@Override
	public List<ProductRanking> saveAll(List<ProductRanking> productRankings) {
		List<ProductRankingEntity> entities = productRankings.stream()
			.map(ProductRankingEntity::fromDomain)
			.toList();

		return productRankingJpaRepository.saveAll(entities)
			.stream()
			.map(ProductRankingEntity::toDomain)
			.toList();
	}

	@Override
	public List<ProductRanking> findAllByRankingDate(LocalDate rankingDate) {
		return productRankingJpaRepository.findAllByRankingDate(rankingDate)
			.stream()
			.map(ProductRankingEntity::toDomain)
			.toList();
	}
}
