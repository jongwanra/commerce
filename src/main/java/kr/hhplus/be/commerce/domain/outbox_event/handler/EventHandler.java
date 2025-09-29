package kr.hhplus.be.commerce.domain.outbox_event.handler;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;

public interface EventHandler {
	boolean support(EventType eventType);

	void handle(Event event);
}
