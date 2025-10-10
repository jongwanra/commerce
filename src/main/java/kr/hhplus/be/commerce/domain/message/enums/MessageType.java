package kr.hhplus.be.commerce.domain.message.enums;

import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;
import kr.hhplus.be.commerce.domain.message.model.message_payload.OrderConfirmedMessagePayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
	ORDER_CONFIRMED("결제가 완료된 주문건", OrderConfirmedMessagePayload.class);
	
	private final String description;
	private final Class<? extends MessagePayload> payloadClass;
}
