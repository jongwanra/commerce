package kr.hhplus.be.commerce.application.product_ranking.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import io.lettuce.core.RedisCommandTimeoutException;
import kr.hhplus.be.commerce.application.product_ranking.ProductRankingSynchronizeScheduler;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingRepository;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import kr.hhplus.be.commerce.global.annotation.UnitTest;
import kr.hhplus.be.commerce.global.time.FixedTimeProvider;
import kr.hhplus.be.commerce.global.time.TimeProvider;

@ExtendWith(MockitoExtension.class)
public class ProductRankingSynchronizeSchedulerUnitTest {
	private ProductRankingSynchronizeScheduler productRankingSynchronizeScheduler;

	@Mock
	private ProductRankingRepository productRankingRepository;
	@Mock
	private ProductRankingStore productRankingStore;
	private TimeProvider timeProvider;

	@BeforeEach
	void setUp() {
		timeProvider = FixedTimeProvider.of(LocalDateTime.now());
		productRankingSynchronizeScheduler = new ProductRankingSynchronizeScheduler(
			productRankingRepository,
			productRankingStore,
			timeProvider
		);
	}

	@UnitTest
	public void 레디스_연결_실패시_예외를_전파하지_않는다() {
		// mock
		given(productRankingStore.readProductIdsDailyTopSelling(timeProvider.today()))
			.willThrow(new RedisConnectionFailureException("Connection failed"));

		// when & then
		assertThatCode(() -> productRankingSynchronizeScheduler.synchronizeToday())
			.doesNotThrowAnyException();
	}

	@UnitTest
	public void 레디스_요청_시_타임아웃이_발생시_예외를_전파하지_않는다() {
		// mock
		given(productRankingStore.readProductIdsDailyTopSelling(timeProvider.today()))
			.willThrow(new RedisCommandTimeoutException("Command timed out"));

		// when & then
		assertThatCode(() -> productRankingSynchronizeScheduler.synchronizeToday())
			.doesNotThrowAnyException();
	}
}
