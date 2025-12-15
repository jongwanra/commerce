package kr.hhplus.be.commerce.infrastructure.persistence.processed_message;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.processed_message.entity.ProcessedMessageEntity;

public interface ProcessedMessageJpaRepository extends JpaRepository<ProcessedMessageEntity, String> {
}
