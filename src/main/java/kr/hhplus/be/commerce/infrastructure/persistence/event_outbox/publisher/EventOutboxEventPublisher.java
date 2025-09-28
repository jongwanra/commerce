package kr.hhplus.be.commerce.infrastructure.persistence.event_outbox.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.event.model.Event;
import kr.hhplus.be.commerce.domain.event.publisher.EventPublisher;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.event_outbox.EventOutboxJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.event_outbox.entity.EventOutboxEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EventOutboxEventPublisher implements EventPublisher {
	private final EventOutboxJpaRepository eventOutboxJpaRepository;
	private final ObjectMapper objectMapper;

	public void publish(Event event) {
		try {
			final String payload = objectMapper.writeValueAsString(event);

			eventOutboxJpaRepository.save(
				EventOutboxEntity.publish(
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
