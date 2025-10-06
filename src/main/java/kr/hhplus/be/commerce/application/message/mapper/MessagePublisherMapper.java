package kr.hhplus.be.commerce.application.message.mapper;

import static java.util.Objects.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.application.message.publisher.MessagePublisher;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.message.enums.MessageType;

@Component
public class MessagePublisherMapper {
	private final Map<MessageType, MessagePublisher> typeToPublisherMap = new ConcurrentHashMap<>();

	public MessagePublisherMapper(List<MessagePublisher> messagePublishers) {
		messagePublishers.forEach((messagePublisher) -> {
			typeToPublisherMap.put(messagePublisher.getSupportingMessageType(), messagePublisher);
		});
	}

	public MessagePublisher get(MessageType messageType) {
		MessagePublisher messagePublisher = typeToPublisherMap.get(messageType);
		if (isNull(messagePublisher)) {
			throw new CommerceException("지원하지 않는 메시지 타입입니다.");
		}

		return messagePublisher;
	}
}
