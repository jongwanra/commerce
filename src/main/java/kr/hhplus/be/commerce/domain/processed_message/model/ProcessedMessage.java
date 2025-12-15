package kr.hhplus.be.commerce.domain.processed_message.model;

import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ProcessedMessage(
	String id,
	LocalDateTime processedAt
) {
	private static final String ID_DELIMITER = ":";

	public static ProcessedMessage of(String key, String topic, String consumerGroupId, LocalDateTime processedAt) {
		return ProcessedMessage.builder()
			.id(generateId(key, topic, consumerGroupId))
			.processedAt(processedAt)
			.build();
	}

	private static String generateId(String key, String topic, String consumerGroupId) {
		return topic + ID_DELIMITER + consumerGroupId + ID_DELIMITER + key;
	}

	@InfrastructureOnly
	public static ProcessedMessage restore(String id, LocalDateTime processedAt) {
		return ProcessedMessage.builder()
			.id(id)
			.processedAt(processedAt)
			.build();
	}
}
