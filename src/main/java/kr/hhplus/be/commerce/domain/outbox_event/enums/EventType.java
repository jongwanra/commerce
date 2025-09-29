package kr.hhplus.be.commerce.domain.outbox_event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
	ORDER_CONFIRMED("결제가 완료된 주문건");
	private final String description;
}
