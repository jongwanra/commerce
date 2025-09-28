package kr.hhplus.be.commerce.infrastructure.config.event_outbox;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.commerce.domain.event.publisher.EventPublisher;
import kr.hhplus.be.commerce.infrastructure.persistence.event_outbox.EventOutboxJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.event_outbox.publisher.EventOutboxEventPublisher;

@Configuration
public class EventOutboxConfig {
	@Bean
	public EventPublisher eventPublisher(EventOutboxJpaRepository eventOutboxJpaRepository) {
		return new EventOutboxEventPublisher(eventOutboxJpaRepository, new ObjectMapper());
	}

}
