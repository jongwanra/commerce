package kr.hhplus.be.commerce.infrastructure.persistence.product;

import static java.util.Objects.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRankingView;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRankingStoreImpl implements ProductRankingStore {

	private final RedisTemplate<String, String> redisTemplate;
	private final ProductRankingKeyGenerator productRankingKeyGenerator;

	@Override
	public void increment(Long productId, Integer salesCount, LocalDate rankingDate, LocalDateTime now) {
		final String key = productRankingKeyGenerator.generate(rankingDate);

		redisTemplate.opsForZSet()
			.incrementScore(key, String.valueOf(productId), salesCount);

		// FIXME: 11/18/25 increment method 호출 시마다 setIfAbsent method를 호출해 레디스 커넥션을 하나 더 사용하는 비용이 불필요한 것 같습니다.
		//  다른 방법은 없는지 고민이 됩니다.
		setTtlIfAbsent(key, rankingDate, now);

	}

	@Override
	public List<ProductRankingView> readProductIdsDailyTopSelling(LocalDate rankingDate) {
		return readProductIdsDailyTopSelling(rankingDate, -1);
	}

	@Override
	public void bulkInsert(List<ProductRanking> productRankings, LocalDate rankingDate, LocalDateTime now) {

		redisTemplate.executePipelined(((RedisCallback<Object>)connection -> {
			final String key = productRankingKeyGenerator.generate(rankingDate);
			productRankings.forEach((ranking) -> connection.zSetCommands().zAdd(
				key.getBytes(),
				ranking.salesCount(),
				String.valueOf(ranking.productId()).getBytes()
			));
			setTtlIfAbsent(key, rankingDate, now);
			return null;
		}));

	}

	@Override
	public List<ProductRankingView> readProductIdsDailyTopSelling(LocalDate rankingDate, int limit) {
		final String key = productRankingKeyGenerator.generate(rankingDate);

		Set<ZSetOperations.TypedTuple<String>> typeTuples = redisTemplate.opsForZSet()
			.reverseRangeWithScores(key, 0, limit);

		return typeTuples.stream()
			.map((tuple) -> {
				final int salesCount = tuple.getScore().intValue();
				final Long productId = Long.parseLong(tuple.getValue());
				ProductRankingView productRanking = new ProductRankingView(rankingDate, productId, salesCount);
				log.debug("productRanking = {}", productRanking);
				return productRanking;
			})
			.toList();
	}

	private void increment(String key, Long productId) {
		redisTemplate.opsForZSet()
			.incrementScore(key, String.valueOf(productId), 1);

	}

	private void setTtlIfAbsent(String key, LocalDate rankingDate, LocalDateTime now) {
		final Long currentTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
		if (isNull(currentTtl) || currentTtl == 0) {
			final long newTtl = calculateSecondsUntilMidnight(rankingDate, now);
			log.debug("Ttl을 설정합니다. key={}, ttl={}", key, newTtl);
			redisTemplate.expire(key, newTtl, TimeUnit.SECONDS);
		}
	}

	private long calculateSecondsUntilMidnight(LocalDate date, LocalDateTime now) {
		LocalDateTime midnight = date.plusDays(1).atStartOfDay();
		return Math.max(0, Duration.between(now, midnight).getSeconds());
	}

}
