package kr.hhplus.be.commerce.domain.product.store;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRankingView;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.product.ProductRankingKeyGenerator;

class ProductRankingStoreIntegrationTest extends AbstractIntegrationTestSupport {
	@Autowired
	private ProductRankingStore productRankingStore;

	@Autowired
	private ProductRankingKeyGenerator productRankingKeyGenerator;

	@IntegrationTest
	void 상품의_판매량을_1_증가시킬_수_있다() {
		// given
		final Long productId = 1L;
		final LocalDate rankingDate = LocalDate.of(2025, 11, 19);
		final LocalDateTime now = rankingDate.atStartOfDay();
		final String key = productRankingKeyGenerator.generate(rankingDate);

		// when
		productRankingStore.increment(productId, 1, rankingDate, now);

		// then
		Double score = redisTemplate.opsForZSet().score(key, String.valueOf(productId));
		Long ttl = redisTemplate.getExpire(key);

		assertThat(score).isEqualTo(1.0);
		assertThat(ttl).isEqualTo(86_400L).as("86,400s -> 1 day");

	}

	@IntegrationTest
	void 상품들의_판매량_기준_내림차순으로_조회할_수_있다() {
		// given
		final LocalDate rankingDate = LocalDate.of(2025, 11, 19);
		final LocalDateTime now = rankingDate.atStartOfDay();

		// 상품 1L의 판매량: 3
		productRankingStore.increment(1L, 3, rankingDate, now);
		// 상품 2L의 판매량: 5
		productRankingStore.increment(2L, 5, rankingDate, now);
		// 상품 3L의 판매량: 1
		productRankingStore.increment(3L, 1, rankingDate, now);

		// when
		List<ProductRankingView> productRankings = productRankingStore.readProductIdsDailyTopSelling(rankingDate, 4);
		// then
		assertThat(productRankings.size()).isEqualTo(3);
		assertThat(productRankings)
			.extracting("productId")
			.containsExactly(2L, 1L, 3L)
			.as("판매량 순: 2 > 3 > 1");
		assertThat(productRankings)
			.extracting("salesCount")
			.containsExactly(5, 3, 1);

	}

}
