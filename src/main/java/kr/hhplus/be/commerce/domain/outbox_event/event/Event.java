package kr.hhplus.be.commerce.domain.outbox_event.event;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventTargetType;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;

public interface Event {

	EventType type();

	EventTargetType targetType();

	Long targetId();

}
