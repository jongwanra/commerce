package kr.hhplus.be.commerce.infrastructure.config.outbox_event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.outbox_event.publisher.EventPublisher;
import kr.hhplus.be.commerce.domain.outbox_event.repository.OutboxEventRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.OutboxEventJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.OutboxEventRepositoryImpl;
import kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.publisher.EventPublisherImpl;

@Configuration
public class OutboxEventConfig {
	@Bean
	public EventPublisher eventPublisher(OutboxEventRepository outboxEventRepository,
		ObjectMapper objectMapper) {
		return new EventPublisherImpl(outboxEventRepository, objectMapper);
	}

	@Bean
	public OutboxEventRepository outboxEventRepository(OutboxEventJpaRepository outboxEventJpaRepository) {
		return new OutboxEventRepositoryImpl(outboxEventJpaRepository);
	}

}
