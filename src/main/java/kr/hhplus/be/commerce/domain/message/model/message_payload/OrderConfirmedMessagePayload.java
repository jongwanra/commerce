package kr.hhplus.be.commerce.domain.message.model.message_payload;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderConfirmedMessagePayload(
	MessageType type,
	Long orderId
) implements MessagePayload {
	public static OrderConfirmedMessagePayload from(Long orderId) {
		return OrderConfirmedMessagePayload.builder()
			.type(MessageType.ORDER_CONFIRMED)
			.orderId(orderId)
			.build();
	}

}
