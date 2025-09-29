package kr.hhplus.be.commerce.domain.outbox_event.handler;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;

@Component
public class OrderConfirmedEventHandler implements EventHandler {
	@Override
	public boolean support(EventType eventType) {
		return EventType.ORDER_CONFIRMED.equals(eventType);
	}

	@Override
	public void handle(Event event) {
		// TODO impl..
	}
}
