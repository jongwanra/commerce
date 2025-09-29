package kr.hhplus.be.commerce.infrastructure.persistence.outbox_event;

import java.util.List;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventStatus;
import kr.hhplus.be.commerce.domain.outbox_event.model.OutboxEvent;
import kr.hhplus.be.commerce.domain.outbox_event.repository.OutboxEventRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.entity.OutboxEventEntity;

public class OutboxEventRepositoryImpl implements OutboxEventRepository {
	private final OutboxEventJpaRepository outboxEventJpaRepository;

	public OutboxEventRepositoryImpl(OutboxEventJpaRepository outboxEventJpaRepository) {
		this.outboxEventJpaRepository = outboxEventJpaRepository;
	}

	@Override
	public OutboxEvent save(OutboxEvent outboxEvent) {
		return outboxEventJpaRepository.save(OutboxEventEntity.fromDomain(outboxEvent))
			.toDomain();
	}

	@Override
	public List<OutboxEvent> findAllByStatusOrderByCreatedAtAscLimit(EventStatus status, int limit) {
		return outboxEventJpaRepository.findAllByStatusOrderByCreatedAtAscLimit(status, limit)
			.stream()
			.map(OutboxEventEntity::toDomain)
			.toList();
	}

	@Override
	public List<OutboxEvent> saveAll(List<OutboxEvent> outboxEvents) {
		List<OutboxEventEntity> entities = outboxEvents
			.stream()
			.map(OutboxEventEntity::fromDomain)
			.toList();

		return outboxEventJpaRepository.saveAll(entities).stream()
			.map(OutboxEventEntity::toDomain)
			.toList();
	}

	@Override
	public List<OutboxEvent> findRetryableFailedEvents(int failedCountThreshold, int limit) {
		return outboxEventJpaRepository.findRetryableFailedEvents(failedCountThreshold, limit)
			.stream()
			.map(OutboxEventEntity::toDomain)
			.toList();
	}
}
