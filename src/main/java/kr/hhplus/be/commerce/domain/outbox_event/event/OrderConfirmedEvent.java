package kr.hhplus.be.commerce.domain.outbox_event.event;

import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventTargetType;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderConfirmedEvent(
	EventType type,
	EventTargetType targetType,
	Long targetId,
	Order order
) implements Event {
	public static OrderConfirmedEvent from(Order order) {
		return OrderConfirmedEvent.builder()
			.type(EventType.ORDER_CONFIRMED)
			.targetType(EventTargetType.ORDER)
			.targetId(order.id())
			.order(order)
			.build();
	}

}
