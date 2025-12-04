package kr.hhplus.be.commerce.application.event;

import org.springframework.kafka.annotation.KafkaListener;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedNotificationEventListener {
	private final SlackSendMessageClient slackSendMessageClient;
	private final ObjectMapper mapper;

	@KafkaListener(topics = "order.placed", groupId = "notification-consumer-group")
	public void handle(String message) {
		try {
			log.debug("[+OrderPlacedNotificationEventListener] 진입: Thread={}", Thread.currentThread().getName());
			OrderPlacedEvent event = mapper.readValue(message, OrderPlacedEvent.class);
			final String messageToSend = "[주문 확정🎉]"
				+ " orderId=" + event.orderId()
				+ " 주문 확정 일시=" + event.occurredAt();

			slackSendMessageClient.send(messageToSend);
		} catch (JsonProcessingException e) {
			log.error("[역직렬화 실패] Kafka 메시지를 OrderPlacedEvent로 변환하는데 실패했습니다. message={}",
				message, e);
		} catch (Exception e) {
			log.error("[알수 없는 에러 발생] 주문 확정 이후, 슬랙 메세지를 보내는데 에러가 발생했습니다.", e);
		}
	}

}
