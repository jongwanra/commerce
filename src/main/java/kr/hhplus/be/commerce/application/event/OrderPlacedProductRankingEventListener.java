package kr.hhplus.be.commerce.application.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private final ObjectMapper mapper;

	/**
	 * {@link OrderPlaceProcessor}의 후처리 로직입니다.
	 */

	@KafkaListener(topics = "order.placed", groupId = "product_ranking_consumer_group")
	public void handle(String message) {
		try {
			log.debug("[+OrderPlacedProductRankingEventListener] 진입: Thread={}", Thread.currentThread().getName());
			OrderPlacedEvent event = mapper.readValue(message, OrderPlacedEvent.class);

			LocalDateTime now = event.occurredAt();
			LocalDate today = now.toLocalDate();

			event.orderLines()
				.forEach(
					(orderLine) -> productRankingStore.increment(orderLine.productId(), orderLine.orderQuantity(),
						today, now));
		} catch (JsonProcessingException e) {
			log.error("[역직렬화 실패] Kafka 메시지를 OrderPlacedEvent로 변환하는데 실패했습니다. message={}",
				message, e);
		} catch (Exception e) {
			log.error("[알수 없는 에러 발생] 주문 확정 이후, 판매량을 증가시키는데 에러가 발생헀습니다.", e);
		}
	}

}
