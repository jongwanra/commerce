package kr.hhplus.be.commerce.application.product_ranking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import io.lettuce.core.RedisCommandTimeoutException;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRankingView;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingRepository;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import kr.hhplus.be.commerce.global.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRankingSynchronizeScheduler {
	private static final int FIVE_MINUTES = 5 * 60 * 1000;

	private final ProductRankingRepository productRankingRepository;
	private final ProductRankingStore productRankingStore;
	private final TimeProvider timeProvider;

	/**
	 * Redis의 실시간 상품 판매량을 Database(product_ranking)에 동기화합니다.
	 * 직전 스케줄러가 끝나고 5분 뒤에 동작합니다.
	 */
	@Scheduled(fixedDelay = FIVE_MINUTES)
	public void synchronizeToday() {
		synchronize(timeProvider.today());
	}

	/**
	 * 어제 집계가 완료된 Redis의 상품 판매량을 Database(product_ranking)에 동기화합니다.
	 * 매일 00:01분에 스케줄러가 동작합니다.
	 * 오늘 레디스에 저장된 상품 판매량은 다음날 01:00에 삭제되도록 TTL이 설정되어 있습니다.
	 * @see kr.hhplus.be.commerce.infrastructure.persistence.product.ProductRankingStoreImpl
	 */
	@Scheduled(cron = "0 1 0 * * *")
	public void synchronizeYesterday() {
		final LocalDate yesterday = timeProvider.today().minusDays(1);
		synchronize(yesterday);
	}

	private void synchronize(LocalDate rankingDate) {
		try {
			log.debug("레디스 -> 데이터베이스의 상품 판매량 랭킹 동기화 작업을 시작합니다.");
			log.debug("rankingDate = {}", rankingDate);
			List<ProductRankingView> currentRankings = productRankingStore.readProductIdsDailyTopSelling(rankingDate);

			Map<Long, ProductRanking> previousRankingMap = productRankingRepository.findAllByRankingDate(rankingDate)
				.stream()
				.collect(Collectors.toMap(ProductRanking::productId, it -> it));

			productRankingRepository.saveAll(synchronize(currentRankings, previousRankingMap));
			log.debug("레디스 -> 데이터베이스의 상품 판매량 랭킹 실시간 동기화 작업을 정상적으로 마쳤습니다.");
		} catch (RedisConnectionFailureException e) {
			log.error("[레디스 시스템 다운] 상품 판매량 랭킹 실시간 동기화 작업을 실패했습니다.", e);
		} catch (RedisCommandTimeoutException e) {
			log.error("[레디스 요청 타임아웃 발생] 상품 판매량 랭킹 실시간 동기화 작업을 실패했습니다.", e);
		} catch (Exception e) {
			log.error("[알 수 없는 예외 발생] 상품 판매량 랭킹 실시간 동기화 작업을 실패했습니다.", e);
		}
	}

	private List<ProductRanking> synchronize(List<ProductRankingView> currentRankings,
		Map<Long, ProductRanking> previousRankingMap) {
		return currentRankings
			.stream()
			.map((currentRanking) -> {
				ProductRanking productRanking = previousRankingMap.getOrDefault(currentRanking.productId(),
					ProductRanking.empty(currentRanking.productId(), currentRanking.rankingDate()));

				log.debug("[동기화 중] productRanking = {}", productRanking);
				return productRanking.renewSalesCount(currentRanking.salesCount());
			})
			.toList();
	}
}
