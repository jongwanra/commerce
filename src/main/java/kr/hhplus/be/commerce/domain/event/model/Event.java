package kr.hhplus.be.commerce.domain.event.model;

import kr.hhplus.be.commerce.domain.event.model.enums.EventTargetType;
import kr.hhplus.be.commerce.domain.event.model.enums.EventType;

public interface Event {

	EventType type();

	EventTargetType targetType();

	Long targetId();
}
