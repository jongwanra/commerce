package kr.hhplus.be.commerce.domain.message.publisher;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;

@Component
public class OrderConfirmedMessagePublisher implements MessagePublisher {

	@Override
	public MessageType getSupportingMessageType() {
		return MessageType.ORDER_CONFIRMED;
	}

	@Override
	public void publish(MessagePayload messagePayload) {
		// TODO impl..
	}
}
