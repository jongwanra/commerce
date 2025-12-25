package kr.hhplus.be.commerce.infrastructure.event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.event.Event;
import kr.hhplus.be.commerce.domain.event.ExternalEventPublisher;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements ExternalEventPublisher {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final EventTopicResolver eventTopicResolver;
	private final ObjectMapper mapper;

	@Override
	public void publish(Event event) {
		try {
			final String topic = eventTopicResolver.resolve(event);
			SendResult<String, String> result = kafkaTemplate.send(topic, event.key(),
					mapper.writeValueAsString(event))
				.get(5, TimeUnit.SECONDS);// 5초 타임아웃

			log.debug("[KafkaEventPublisher] 메시지 발행 성공. Topic={}, Partition={}, Offset={}", topic,
				result.getRecordMetadata().partition(), result.getRecordMetadata().offset());

		} catch (JsonProcessingException e) {
			log.error("[KafkaEventPublisher] Event 객체를 JSON 문자열로 직렬화하는데 에러가 발생했습니다.", e);
			throw new CommerceException(e.getMessage());
		} catch (TimeoutException e) {
			log.error("[KafkaEventPublisher] Kafka 메시지 발행 타임아웃", e);
			throw new CommerceException("이벤트 발행 타임아웃");
		} catch (Exception e) {
			log.error("[KafkaEventPublisher] 이벤트를 발행하는데 예상하지 못한 에러가 발생했습니다.", e);
			throw new CommerceException(e.getMessage());
		}
	}
}
