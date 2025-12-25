package kr.hhplus.be.commerce.application.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.order.event.OrderPlacedEvent;
import kr.hhplus.be.commerce.infrastructure.client.slack.SlackSendMessageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link OrderPlaceProcessor}의 후처리 로직입니다.
 *
 * Slack 알림 전송은 부가적인 로직이기 때문에 실패 시 로깅 후, 재처리는 생략합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedNotificationEventListener {
	private static final String TOPIC = "order.placed";
	private static final String CONSUMER_GROUP_ID = "notification-consumer-group";

	private final SlackSendMessageClient slackSendMessageClient;
	private final ObjectMapper mapper;

	@KafkaListener(topics = TOPIC, groupId = CONSUMER_GROUP_ID)
	public void handle(String message, Acknowledgment ack) {
		try {
			log.debug("[+OrderPlacedNotificationEventListener] 진입: Thread={}", Thread.currentThread().getName());
			OrderPlacedEvent event = mapper.readValue(message, OrderPlacedEvent.class);
			slackSendMessageClient.send(generateMessageToSend(event));
			ack.acknowledge();
		} catch (JsonProcessingException e) {
			log.error("[역직렬화 실패] Kafka 메시지를 OrderPlacedEvent로 변환하는데 실패했습니다. message={}",
				message, e);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("[알수 없는 에러 발생] 주문 확정 이후, 슬랙 메세지를 보내는데 에러가 발생했습니다.", e);
			ack.acknowledge();
		}
	}

	private String generateMessageToSend(OrderPlacedEvent event) {
		return "[주문 확정🎉]"
			+ " orderId=" + event.orderId()
			+ " 주문 확정 일시=" + event.occurredAt();
	}

}
