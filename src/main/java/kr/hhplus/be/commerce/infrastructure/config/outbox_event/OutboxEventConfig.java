package kr.hhplus.be.commerce.infrastructure.config.outbox_event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.outbox_event.recorder.EventRecorder;
import kr.hhplus.be.commerce.domain.outbox_event.repository.OutboxEventRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.OutboxEventJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.outbox_event.OutboxEventRepositoryImpl;

@Configuration
public class OutboxEventConfig {
	@Bean
	public EventRecorder eventPublisher(OutboxEventRepository outboxEventRepository,
		ObjectMapper objectMapper) {
		return new EventRecorder(outboxEventRepository, objectMapper);
	}

	@Bean
	public OutboxEventRepository outboxEventRepository(OutboxEventJpaRepository outboxEventJpaRepository) {
		return new OutboxEventRepositoryImpl(outboxEventJpaRepository);
	}

}
