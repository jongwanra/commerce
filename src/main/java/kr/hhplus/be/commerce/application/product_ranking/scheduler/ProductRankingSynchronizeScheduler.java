package kr.hhplus.be.commerce.application.product_ranking.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRanking;
import kr.hhplus.be.commerce.domain.product_ranking.model.ProductRankingView;
import kr.hhplus.be.commerce.domain.product_ranking.repository.ProductRankingRepository;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import kr.hhplus.be.commerce.global.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 5분을 주기로 Redis의 상품 판매량 랭킹을 Database의 product_ranking에 동기화합니다.
 */

@Service
@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"local"})
public class ProductRankingSynchronizeScheduler {
	private static final int FIVE_MINUTES = 5 * 60 * 1000;

	private final ProductRankingRepository productRankingRepository;
	private final ProductRankingStore productRankingStore;
	private final TimeProvider timeProvider;

	@Scheduled(fixedDelay = FIVE_MINUTES)
	public void execute() {
		try {
			log.debug("레디스 -> 데이터베이스의 상품 판매량 랭킹 동기화 작업을 시작합니다.");
			LocalDate today = timeProvider.today();
			log.debug("today = {}", today);
			List<ProductRankingView> currentRankings = productRankingStore.readProductIdsDailyTopSelling(today);

			Map<Long, ProductRanking> previousRankingMap = productRankingRepository.findAllByRankingDate(today)
				.stream()
				.collect(Collectors.toMap(ProductRanking::productId, it -> it));

			productRankingRepository.saveAll(synchronize(currentRankings, previousRankingMap));
			log.debug("레디스 -> 데이터베이스의 상품 판매량 랭킹 동기화 작업을 정상적으로 마쳤습니다.");
		} catch (RedisConnectionFailureException e) {
			log.error("[레디스 시스템 다운] 상품 판매량 랭킹 동기화 작업을 실패했습니다.", e);
		} catch (Exception e) {
			log.error("예상하지 못한 이유로, Redis의 상품 판매량 랭킹을 데이터베이스에 동기화 하는데 실패했습니다.", e);
		}
	}

	private List<ProductRanking> synchronize(List<ProductRankingView> currentRankings,
		Map<Long, ProductRanking> previousRankingMap) {
		return currentRankings
			.stream()
			.map((currentRanking) -> {
				ProductRanking productRanking = previousRankingMap.getOrDefault(currentRanking.productId(),
					ProductRanking.empty(currentRanking.productId()));

				return productRanking.renewSalesCount(currentRanking.salesCount());
			})
			.toList();
	}
}
