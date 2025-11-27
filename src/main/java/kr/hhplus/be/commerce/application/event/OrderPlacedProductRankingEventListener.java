package kr.hhplus.be.commerce.application.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.order.event.OrderPlacedEvent;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedProductRankingEventListener {
	private final ProductRankingStore productRankingStore;

	/**
	 * {@link OrderPlaceProcessor}의 후처리 로직입니다.
	 */

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(OrderPlacedEvent event) {
		try {
			log.debug("[+OrderPlacedProductRankingEventListener] 진입: Thread={}", Thread.currentThread().getName());
			LocalDateTime now = event.occurredAt();
			LocalDate today = now.toLocalDate();

			event.orderLines()
				.forEach(
					(orderLine) -> productRankingStore.increment(orderLine.productId(), orderLine.orderQuantity(),
						today,
						now));
		} catch (Exception e) {
			log.error("[알수 없는 에러 발생] 주문 확정 이후, 판매량을 증가시키는데 에러가 발생헀습니다.", e);
		}
	}

}
