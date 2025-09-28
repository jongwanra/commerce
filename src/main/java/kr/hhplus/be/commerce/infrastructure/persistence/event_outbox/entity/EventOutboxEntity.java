package kr.hhplus.be.commerce.infrastructure.persistence.event_outbox.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.event.model.enums.EventStatus;
import kr.hhplus.be.commerce.domain.event.model.enums.EventTargetType;
import kr.hhplus.be.commerce.domain.event.model.enums.EventType;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_outbox")
public class EventOutboxEntity extends BaseTimeEntity {
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

	@Builder
	private EventOutboxEntity(Long id, Long targetId, EventTargetType targetType, EventType type,
		EventStatus status,
		String payload, LocalDateTime sentAt, LocalDateTime failedAt, String failedReason) {
		this.id = id;
		this.targetId = targetId;
		this.targetType = targetType;
		this.type = type;
		this.status = status;
		this.payload = payload;
		this.sentAt = sentAt;
		this.failedAt = failedAt;
		this.failedReason = failedReason;
	}

	public static EventOutboxEntity publish(EventType eventType, Long targetId, EventTargetType eventTargetType,
		String payload) {
		return EventOutboxEntity
			.builder()
			.targetId(targetId)
			.targetType(eventTargetType)
			.type(eventType)
			.status(EventStatus.PENDING)
			.payload(payload)
			.build();
	}
	
}
