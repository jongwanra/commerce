package kr.hhplus.be.commerce.domain.outbox_event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventTargetType {
	ORDER, PAYMENT
}
