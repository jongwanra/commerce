package kr.hhplus.be.commerce.domain.message.publisher;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;

public interface MessagePublisher {
	MessageType getSupportingMessageType();

	void publish(MessagePayload messagePayload);

}
