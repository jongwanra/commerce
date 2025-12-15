package kr.hhplus.be.commerce.infrastructure.persistence.processed_message.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.processed_message.model.ProcessedMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "processed_message")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ProcessedMessageEntity {
	@Id
	private String messageId;

	private LocalDateTime processedAt;

	public static ProcessedMessageEntity fromDomain(ProcessedMessage processedMessage) {
		return ProcessedMessageEntity.builder()
			.messageId(processedMessage.messageId())
			.processedAt(processedMessage.processedAt())
			.build();
	}

	public ProcessedMessage toDomain() {
		return ProcessedMessage.restore(this.messageId, this.processedAt);
	}
}
