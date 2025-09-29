package kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventStatus;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventTargetType;
import kr.hhplus.be.commerce.domain.outbox_event.enums.EventType;
import kr.hhplus.be.commerce.domain.outbox_event.model.OutboxEvent;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_event")
@Getter
public class OutboxEventEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long targetId;
	@Enumerated(EnumType.STRING)
	private EventTargetType targetType;

	@Enumerated(EnumType.STRING)
	private EventType type;
	@Enumerated(EnumType.STRING)
	private EventStatus status;
	private String payload;
	private LocalDateTime sentAt;
	private LocalDateTime failedAt;
	private String failedReason;
	private Integer failedCount;

	@Builder
	private OutboxEventEntity(Long id, Long targetId, EventTargetType targetType, EventType type,
		EventStatus status,
		String payload, LocalDateTime sentAt, LocalDateTime failedAt, String failedReason, Integer failedCount) {
		this.id = id;
		this.targetId = targetId;
		this.targetType = targetType;
		this.type = type;
		this.status = status;
		this.payload = payload;
		this.sentAt = sentAt;
		this.failedAt = failedAt;
		this.failedReason = failedReason;
		this.failedCount = failedCount;
	}

	public static OutboxEventEntity fromDomain(OutboxEvent outboxEvent) {
		return OutboxEventEntity.builder()
			.id(outboxEvent.id())
			.targetId(outboxEvent.targetId())
			.targetType(outboxEvent.targetType())
			.type(outboxEvent.type())
			.status(outboxEvent.status())
			.payload(outboxEvent.payload())
			.sentAt(outboxEvent.sentAt())
			.failedAt(outboxEvent.failedAt())
			.failedReason(outboxEvent.failedReason())
			.failedCount(outboxEvent.failedCount())
			.build();
	}

	public OutboxEvent toDomain() {
		return OutboxEvent.restore(id, targetId, targetType, type, status, payload, sentAt, failedAt, failedReason,
			failedCount);
	}
}
