package kr.hhplus.be.commerce.domain.outbox_event.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventStatus;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventTargetType;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.event.Event;
import kr.hhplus.be.commerce.domain.outbox_event.event.OrderConfirmedEvent;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OutboxEvent(
	Long id,
	Long targetId,
	EventTargetType targetType,
	EventType type,
	EventStatus status,
	String payload,
	LocalDateTime sentAt,
	LocalDateTime failedAt,
	String failedReason,
	Integer failedCount
) {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final int FAILED_COUNT_THRESHOLD = 3;

	@InfrastructureOnly
	public static OutboxEvent restore(Long id, Long targetId, EventTargetType targetType, EventType type,
		EventStatus status, String payload, LocalDateTime sentAt, LocalDateTime failedAt, String failedReason,
		Integer failedCount) {
		return OutboxEvent.builder()
			.id(id)
			.targetId(targetId)
			.targetType(targetType)
			.type(type)
			.status(status)
			.payload(payload)
			.sentAt(sentAt)
			.failedAt(failedAt)
			.failedReason(failedReason)
			.failedCount(failedCount)
			.build();
	}

	public static OutboxEvent ofPending(EventType type, Long targetId, EventTargetType targetType, String payload) {
		return OutboxEvent.builder()
			.targetId(targetId)
			.targetType(targetType)
			.type(type)
			.status(EventStatus.PENDING)
			.payload(payload)
			.sentAt(null)
			.failedAt(null)
			.failedReason("")
			.failedCount(0)
			.build();
	}

	public Event toEvent() {
		try {
			if (type.equals(EventType.ORDER_CONFIRMED)) {
				return objectMapper.readValue(payload, OrderConfirmedEvent.class);
			}

			throw new CommerceException("일치한 Event가 존재하지 않습니다. [ " + type + " ]");

		} catch (Exception e) {
			throw new CommerceException(e.getMessage());
		}
	}

	public OutboxEvent failed(String failedReason) {
		final int newFailedCount = failedCount + 1;
		// 3번 실패했을 경우 DEAD_LETTER 상태로 변경
		EventStatus newEventStatus =
			newFailedCount >= FAILED_COUNT_THRESHOLD ? EventStatus.DEAD_LETTER : EventStatus.FAILED;

		return OutboxEvent.builder()
			.id(id)
			.targetId(targetId)
			.targetType(targetType)
			.type(type)
			.status(newEventStatus)
			.payload(payload)
			.sentAt(sentAt)
			.failedAt(LocalDateTime.now())
			.failedReason(failedReason.trim())
			.failedCount(newFailedCount)
			.build();

	}

	public OutboxEvent published() {
		return OutboxEvent.builder()
			.id(id)
			.targetId(targetId)
			.targetType(targetType)
			.type(type)
			.status(EventStatus.PUBLISHED)
			.payload(payload)
			.sentAt(LocalDateTime.now())
			.failedAt(failedAt)
			.failedReason(failedReason)
			.failedCount(failedCount)
			.build();
	}
}
