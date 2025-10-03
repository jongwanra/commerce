package kr.hhplus.be.commerce.application.event.publisher;

import kr.hhplus.be.commerce.domain.event.Event;

public interface EventPublisher {
	void publish(Event event);
}
