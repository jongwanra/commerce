package kr.hhplus.be.commerce.domain.outbox_event.repository;

import java.util.List;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventStatus;
import kr.hhplus.be.commerce.domain.outbox_event.model.OutboxEvent;

public interface OutboxEventRepository {
	OutboxEvent save(OutboxEvent outboxEvent);

	List<OutboxEvent> findAllByStatusOrderByCreatedAtAscLimit(EventStatus status, int limit);

	List<OutboxEvent> saveAll(List<OutboxEvent> outboxEvents);

	List<OutboxEvent> findRetryableFailedEvents(int failedCountThreshold, int limit);
}
