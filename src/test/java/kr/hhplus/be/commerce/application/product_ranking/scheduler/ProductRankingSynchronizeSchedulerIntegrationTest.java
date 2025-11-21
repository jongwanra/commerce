package kr.hhplus.be.commerce.application.product_ranking.scheduler;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingRepository;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.global.time.FixedTimeProvider;
import kr.hhplus.be.commerce.global.time.TimeProvider;
import kr.hhplus.be.commerce.infrastructure.persistence.product_ranking.ProductRankingEntity;

class ProductRankingSynchronizeSchedulerIntegrationTest extends AbstractIntegrationTestSupport {
	private ProductRankingSynchronizeScheduler productRankingSynchronizeScheduler;

	@Autowired
	private ProductRankingRepository productRankingRepository;

	@Autowired
	private ProductRankingStore productRankingStore;

	private TimeProvider timeProvider;

	@BeforeEach
	void setUp() {
		timeProvider = FixedTimeProvider.of(LocalDate.of(2025, 11, 19));
		productRankingSynchronizeScheduler = new ProductRankingSynchronizeScheduler(productRankingRepository,
			productRankingStore,
			timeProvider);
	}

	@IntegrationTest
	public void 상품_3개의_판매량을_정상적으로_동기화할_수_있다() {
		// given
		final LocalDate rankingDate = timeProvider.today();
		final LocalDateTime now = timeProvider.now();

		// 상품 1L의 판매량: 1
		for (int index = 0; index < 1; index++) {
			productRankingStore.increment(List.of(1L), rankingDate, now);
		}

		// 상품 2L의 판매량: 5
		for (int index = 0; index < 5; index++) {
			productRankingStore.increment(List.of(2L), rankingDate, now);
		}

		// 상품 3L의 판매량: 10
		for (int index = 0; index < 10; index++) {
			productRankingStore.increment(List.of(3L), rankingDate, now);
		}

		// when
		productRankingSynchronizeScheduler.execute();

		// then
		List<ProductRankingEntity> productRankingEntities = productRankingJpaRepository.findAll();
		assertThat(productRankingEntities.size()).isEqualTo(3);
		assertThat(productRankingEntities)
			.extracting("productId")
			.containsExactlyInAnyOrder(1L, 2L, 3L);

		assertThat(productRankingEntities)
			.extracting("salesCount")
			.containsExactlyInAnyOrder(1, 5, 10);

		assertThat(productRankingEntities)
			.extracting("rankingDate")
			.containsOnly(LocalDate.of(2025, 11, 19));

	}

}
