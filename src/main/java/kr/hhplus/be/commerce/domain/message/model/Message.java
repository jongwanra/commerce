package kr.hhplus.be.commerce.domain.message.model;

import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record Message(
	Long id,
	Long targetId,
	MessageTargetType targetType,
	MessageType type,
	MessageStatus status,
	MessagePayload payload,
	LocalDateTime publishedAt,
	LocalDateTime failedAt,
	String failedReason,
	Integer failedCount
) {
	private static final int FAILED_COUNT_THRESHOLD = 3;

	@InfrastructureOnly
	public static Message restore(Long id, Long targetId, MessageTargetType targetType, MessageType type,
		MessageStatus status, MessagePayload payload, LocalDateTime sentAt, LocalDateTime failedAt, String failedReason,
		Integer failedCount) {
		return Message.builder()
			.id(id)
			.targetId(targetId)
			.targetType(targetType)
			.type(type)
			.status(status)
			.payload(payload)
			.publishedAt(sentAt)
			.failedAt(failedAt)
			.failedReason(failedReason)
			.failedCount(failedCount)
			.build();
	}

	public static Message ofPending(Long targetId, MessageTargetType targetType,
		MessagePayload payload) {
		return Message.builder()
			.targetId(targetId)
			.targetType(targetType)
			.type(payload.type())
			.status(MessageStatus.PENDING)
			.payload(payload)
			.publishedAt(null)
			.failedAt(null)
			.failedReason("")
			.failedCount(0)
			.build();
	}

	public Message failed(String failedReason, LocalDateTime failedAt) {
		final int newFailedCount = failedCount + 1;
		// 3번 실패했을 경우 DEAD_LETTER 상태로 변경
		final MessageStatus newStatus =
			newFailedCount >= FAILED_COUNT_THRESHOLD ? MessageStatus.DEAD_LETTER : MessageStatus.FAILED;

		return Message.builder()
			.id(id)
			.targetId(targetId)
			.targetType(targetType)
			.type(type)
			.status(newStatus)
			.payload(payload)
			.publishedAt(publishedAt)
			.failedAt(failedAt)
			.failedReason(failedReason.trim())
			.failedCount(newFailedCount)
			.build();

	}

	public Message published(LocalDateTime publishedAt) {
		return Message.builder()
			.id(id)
			.targetId(targetId)
			.targetType(targetType)
			.type(type)
			.status(MessageStatus.PUBLISHED)
			.payload(payload)
			.publishedAt(publishedAt)
			.failedAt(failedAt)
			.failedReason(failedReason)
			.failedCount(failedCount)
			.build();
	}
}
