package kr.hhplus.be.commerce.domain.processed_message.model;

import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ProcessedMessage(
	String messageId,
	LocalDateTime processedAt
) {
	private static final String MESSAGE_ID_DELIMITER = ":";

	public static ProcessedMessage of(String key, String topic, String consumerGroupId, LocalDateTime processedAt) {
		return ProcessedMessage.builder()
			.messageId(generateMessageId(key, topic, consumerGroupId))
			.processedAt(processedAt)
			.build();
	}

	private static String generateMessageId(String key, String topic, String consumerGroupId) {
		return topic + MESSAGE_ID_DELIMITER + consumerGroupId + MESSAGE_ID_DELIMITER + key;
	}

	@InfrastructureOnly
	public static ProcessedMessage restore(String messageId, LocalDateTime processedAt) {
		return ProcessedMessage.builder()
			.messageId(messageId)
			.processedAt(processedAt)
			.build();
	}
}
