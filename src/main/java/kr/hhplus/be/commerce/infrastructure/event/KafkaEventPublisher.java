package kr.hhplus.be.commerce.infrastructure.event;

import org.springframework.kafka.core.KafkaTemplate;
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
			kafkaTemplate.send(topic, event.key(), mapper.writeValueAsString(event));
		} catch (JsonProcessingException e) {
			log.error("[KafkaEventPublisher] Event 객체를 JSON 문자열로 직렬화하는데 에러가 발생했습니다.", e);
			throw new CommerceException(e.getMessage());
		}
	}
}
