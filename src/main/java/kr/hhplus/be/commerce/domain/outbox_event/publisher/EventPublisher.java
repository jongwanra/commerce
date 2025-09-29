package kr.hhplus.be.commerce.domain.outbox_event.publisher;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;

public interface EventPublisher {
	EventType getSupportingEventType();

	void publish(Event event);

}
