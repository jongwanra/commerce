package kr.hhplus.be.commerce.domain.outbox_event.recorder;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;
import kr.hhplus.be.commerce.domain.outbox_event.model.OutboxEvent;
import kr.hhplus.be.commerce.domain.outbox_event.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventRecorder {
	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public void record(Event event) {
		try {
			final String payload = objectMapper.writeValueAsString(event);

			outboxEventRepository.save(
				OutboxEvent.ofPending(
					event.type(),
					event.targetId(),
					event.targetType(),
					payload
				)
			);

		} catch (JsonProcessingException e) {
			log.error("이벤트를 저장하는데 실패했습니다: {}", e);
			throw new CommerceException(e.getMessage());
		}

	}
}
