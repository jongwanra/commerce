package kr.hhplus.be.commerce.infrastructure.persistence.event_outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.event_outbox.entity.EventOutboxEntity;

public interface EventOutboxJpaRepository extends JpaRepository<EventOutboxEntity, Long> {
}
