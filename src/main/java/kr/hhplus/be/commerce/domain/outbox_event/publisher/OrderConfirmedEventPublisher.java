package kr.hhplus.be.commerce.domain.outbox_event.publisher;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;

@Component
public class OrderConfirmedEventPublisher implements EventPublisher {

	@Override
	public EventType getSupportingEventType() {
		return EventType.ORDER_CONFIRMED;
	}

	@Override
	public void publish(Event event) {
		// TODO impl..
	}
}
