package kr.hhplus.be.commerce.infrastructure.persistence.product_ranking;

import static kr.hhplus.be.commerce.infrastructure.persistence.product_ranking.QProductRankingEntity.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingReader;

@Repository
public class ProductRankingReaderImpl implements ProductRankingReader {
	private final JPAQueryFactory queryFactory;

	public ProductRankingReaderImpl(EntityManager entityManager) {
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	@Override
	public List<Long> readProductIdsByRankingDateOrderBySalesCountDesc(LocalDate rankingDate, int limit) {
		return queryFactory
			.select(productRankingEntity.productId)
			.from(productRankingEntity)
			.where(productRankingEntity.rankingDate.eq(rankingDate))
			.orderBy(productRankingEntity.salesCount.desc())
			.limit(limit)
			.fetch();
	}
}
