package kr.hhplus.be.commerce.application.message.publisher;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;

public interface MessagePublisher<T extends MessagePayload> {
	MessageType getSupportingMessageType();

	void publish(T messagePayload);

}
