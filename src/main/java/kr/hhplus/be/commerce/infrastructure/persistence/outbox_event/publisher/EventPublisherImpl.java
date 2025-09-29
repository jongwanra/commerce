package kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;
import kr.hhplus.be.commerce.domain.outbox_event.model.OutboxEvent;
import kr.hhplus.be.commerce.domain.outbox_event.publisher.EventPublisher;
import kr.hhplus.be.commerce.domain.outbox_event.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {
	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public void publish(Event event) {
		try {
			final String payload = objectMapper.writeValueAsString(event);

			outboxEventRepository.save(
				OutboxEvent.publish(
					event.type(),
					event.targetId(),
					event.targetType(),
					payload
				)
			);

		} catch (JsonProcessingException e) {
			log.error("이벤트를 발행하는데 실패했습니다: {}", e);
			throw new CommerceException(e.getMessage());
		}

	}
}
