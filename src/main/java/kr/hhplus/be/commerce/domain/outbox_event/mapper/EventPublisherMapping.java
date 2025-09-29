package kr.hhplus.be.commerce.domain.outbox_event.mapper;

import static java.util.Objects.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.publisher.EventPublisher;

@Component
public class EventPublisherMapping {
	private final Map<EventType, EventPublisher> eventTypeToEventPublisherMap = new ConcurrentHashMap<>();

	public EventPublisherMapping(List<EventPublisher> eventPublishers) {
		eventPublishers.forEach((eventPublisher) -> {
			eventTypeToEventPublisherMap.put(eventPublisher.getSupportingEventType(), eventPublisher);
		});
	}

	public EventPublisher get(EventType eventType) {
		EventPublisher eventPublisher = eventTypeToEventPublisherMap.get(eventType);
		if (isNull(eventPublisher)) {
			throw new CommerceException("지원하지 않는 이벤트입니다.");
		}

		return eventPublisher;
	}
}
