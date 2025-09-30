package kr.hhplus.be.commerce.domain.message.model.message_payload;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.order.model.Order;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderConfirmedMessagePayload(
	MessageType type,
	Order order
) implements MessagePayload {
	public static OrderConfirmedMessagePayload from(Order order) {
		return OrderConfirmedMessagePayload.builder()
			.type(MessageType.ORDER_CONFIRMED)
			.order(order)
			.build();
	}

}
