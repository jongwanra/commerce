package kr.hhplus.be.commerce.infrastructure.persistence.message.entity;

import static java.util.Objects.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;

@Converter
public class MessagePayloadConverter implements AttributeConverter<MessagePayload, String> {
	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(MessagePayload messagePayload) {
		if (isNull(messagePayload)) {
			throw new CommerceException("messagePayload is required");
		}

		try {
			return mapper.writeValueAsString(messagePayload);
		} catch (JsonProcessingException e) {
			throw new CommerceException(e.getMessage());
		}
	}

	@Override
	public MessagePayload convertToEntityAttribute(String payloadText) {
		if (isNull(payloadText) || payloadText.isEmpty()) {
			throw new CommerceException("payloadText is required");
		}

		try {
			JsonNode jsonNode = mapper.readTree(payloadText);
			final String typeText = jsonNode.get("type").asText();
			MessageType messageType = MessageType.valueOf(typeText);
			return mapper.readValue(payloadText, messageType.getPayloadClass());
		} catch (JsonProcessingException e) {
			throw new CommerceException(e.getMessage());
		}
	}
}
