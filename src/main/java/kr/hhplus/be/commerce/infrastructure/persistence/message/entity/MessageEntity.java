package kr.hhplus.be.commerce.infrastructure.persistence.message.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "message")
@Getter
public class MessageEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long targetId;
	@Enumerated(EnumType.STRING)
	private MessageTargetType targetType;

	@Enumerated(EnumType.STRING)
	private MessageType type;
	@Enumerated(EnumType.STRING)
	private MessageStatus status;

	@Convert(converter = MessagePayloadConverter.class)
	private MessagePayload payload;
	private LocalDateTime publishedAt;
	private LocalDateTime failedAt;
	private String failedReason;
	private Integer failedCount;

	@Builder
	private MessageEntity(Long id, Long targetId, MessageTargetType targetType, MessageType type,
		MessageStatus status,
		MessagePayload payload, LocalDateTime publishedAt, LocalDateTime failedAt, String failedReason,
		Integer failedCount) {
		this.id = id;
		this.targetId = targetId;
		this.targetType = targetType;
		this.type = type;
		this.status = status;
		this.payload = payload;
		this.publishedAt = publishedAt;
		this.failedAt = failedAt;
		this.failedReason = failedReason;
		this.failedCount = failedCount;
	}

	public static MessageEntity fromDomain(Message message) {
		return MessageEntity.builder()
			.id(message.id())
			.targetId(message.targetId())
			.targetType(message.targetType())
			.type(message.type())
			.status(message.status())
			.payload(message.payload())
			.publishedAt(message.publishedAt())
			.failedAt(message.failedAt())
			.failedReason(message.failedReason())
			.failedCount(message.failedCount())
			.build();
	}

	public Message toDomain() {
		return Message.restore(id, targetId, targetType, type, status, payload, publishedAt, failedAt, failedReason,
			failedCount);
	}
}
