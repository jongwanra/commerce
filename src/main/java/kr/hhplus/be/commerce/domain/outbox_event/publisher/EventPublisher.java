package kr.hhplus.be.commerce.domain.outbox_event.publisher;

import kr.hhplus.be.commerce.domain.outbox_event.event.Event;

public interface EventPublisher {
	void publish(Event event);
}
