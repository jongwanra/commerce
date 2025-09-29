package kr.hhplus.be.commerce.application.outbox_event.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.handler.EventHandler;
import kr.hhplus.be.commerce.domain.outbox_event.model.OutboxEvent;
import kr.hhplus.be.commerce.domain.outbox_event.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRetryableFailedEventsScheduler {
	private static final int BATCH_SIZE = 100;
	private static final int ONE_MINUTE = 60 * 1000;

	private final OutboxEventRepository outboxEventRepository;
	private final List<EventHandler> eventHandlers;

	@Scheduled(fixedDelay = ONE_MINUTE)
	@Transactional
	public void execute() {
		List<OutboxEvent> outboxEvents = outboxEventRepository.findRetryableFailedEvents(4, BATCH_SIZE)
			.stream()
			.map((outboxEvent) -> {
				try {
					EventHandler eventHandler = getEventHandler(outboxEvent.type());
					eventHandler.handle(outboxEvent.toEvent());
					return outboxEvent.sent();
				} catch (Exception e) {
					return outboxEvent.fail(e.getMessage());
				}

			})
			.toList();

		outboxEventRepository.saveAll(outboxEvents);

	}

	private EventHandler getEventHandler(EventType eventType) {
		for (EventHandler eventHandler : eventHandlers) {
			if (eventHandler.support(eventType)) {
				return eventHandler;
			}
		}
		throw new CommerceException("Event handler not found for event type " + eventType);
	}

}
