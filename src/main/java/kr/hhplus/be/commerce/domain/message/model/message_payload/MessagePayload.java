package kr.hhplus.be.commerce.domain.message.model.message_payload;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;

public interface MessagePayload {
	MessageType type();
}
