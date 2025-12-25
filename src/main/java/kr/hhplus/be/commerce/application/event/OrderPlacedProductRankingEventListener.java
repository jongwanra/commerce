package kr.hhplus.be.commerce.application.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.order.event.OrderPlacedEvent;
import kr.hhplus.be.commerce.domain.processed_message.model.ProcessedMessage;
import kr.hhplus.be.commerce.domain.processed_message.repository.ProcessedMessageRepository;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedProductRankingEventListener {
	private static final String CONSUMER_GROUP_ID = "product_ranking_consumer_group";
	private static final String TOPIC = "order.placed";

	private final ProductRankingStore productRankingStore;
	private final ObjectMapper mapper;
	private final ProcessedMessageRepository processedMessageRepository;
	private final TransactionTemplate transactionTemplate;

	/**
	 * {@link OrderPlaceProcessor}의 후처리 로직입니다.
	 */
	@RetryableTopic(
		attempts = "4", // 최초 1회 + 추가 3회 = 4회,
		backoff = @Backoff(delay = 1000, multiplier = 2.0), // 초기 간격은 1초로, 재시도 간격 2배씩 증가,
		dltTopicSuffix = ".dlq",
		exclude = {JsonProcessingException.class} // JsonProcessingException을 제외하고는 재시도를 진행합니다.
	)
	@KafkaListener(topics = TOPIC, groupId = CONSUMER_GROUP_ID)
	public void handle(String message, Acknowledgment ack) throws JsonProcessingException {
		try {
			OrderPlacedEvent event = mapper.readValue(message, OrderPlacedEvent.class);
			transactionTemplate.executeWithoutResult((status) -> processMessage(event));
			ack.acknowledge();
		} catch (DataIntegrityViolationException e) {
			log.warn("[Idempotency Skip] 중복 메시지입니다. Ack 처리: message={}", message);
			ack.acknowledge();
		} catch (JsonProcessingException e) {
			log.error("[역직렬화 실패] Kafka 메시지를 OrderPlacedEvent로 변환하는데 실패했습니다. message={}",
				message, e);
			throw e; // excluded 설정에 따라, DLQ로 바로 전달합니다.
		} catch (Exception e) {
			log.error("[알수 없는 에러 발생] 주문 확정 이후, 판매량을 증가시키는데 에러가 발생헀습니다.", e);
			throw e;
		}
	}

	private void processMessage(OrderPlacedEvent event) {
		LocalDateTime now = event.occurredAt();
		LocalDate today = now.toLocalDate();

		ProcessedMessage processedMessage = ProcessedMessage.of(event.key(), TOPIC, CONSUMER_GROUP_ID,
			event.occurredAt());
		if (processedMessageRepository.existsByMessageId(processedMessage.id())) {
			return;
		}

		processedMessageRepository.save(processedMessage);

		event.orderLines()
			.forEach(
				(orderLine) -> productRankingStore.increment(orderLine.productId(), orderLine.orderQuantity(),
					today, now));
		
	}

	/**
	 * 최종 실패 메시지를 처리합니다.
	 */
	@DltHandler
	public void handleDlt(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, Acknowledgment ack) {
		log.error("[DLQ REACHED] 최종 처리 실패. Topic={}, Message={}", topic, message);
		// TODO: 알림 발송 및 별도 백업 DB에 저장이 필요합니다.
		ack.acknowledge(); // DLQ 메시지를 처리했으므로 커밋합니다.
	}
}
