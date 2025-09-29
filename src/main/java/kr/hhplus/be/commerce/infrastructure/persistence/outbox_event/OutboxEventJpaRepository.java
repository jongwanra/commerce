package kr.hhplus.be.commerce.infrastructure.persistence.outbox_event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hhplus.be.commerce.domain.outbox_event.enums.EventStatus;
import kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.entity.OutboxEventEntity;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {
	@Query("SELECT oe FROM OutboxEventEntity oe WHERE oe.status= :status ORDER BY oe.createdAt ASC LIMIT :limit")
	List<OutboxEventEntity> findAllByStatusOrderByCreatedAtAscLimit(@Param("status") EventStatus status,
		@Param("limit") int limit);

	@Query("SELECT oe FROM OutboxEventEntity oe WHERE oe.status= 'FAILED' AND oe.failedCount < :failedCountThreshold ORDER BY oe.createdAt ASC LIMIT :limit")
	List<OutboxEventEntity> findRetryableFailedEvents(@Param("failedCountThreshold") int failedCountThreshold,
		@Param("limit") int limit);
}
