package kr.hhplus.be.commerce.domain.event.publisher;

import kr.hhplus.be.commerce.domain.event.model.Event;

public interface EventPublisher {
	void publish(Event event);

}
