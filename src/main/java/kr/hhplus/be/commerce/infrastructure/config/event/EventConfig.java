package kr.hhplus.be.commerce.infrastructure.config.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.hhplus.be.commerce.application.event.publisher.EventPublisher;
import kr.hhplus.be.commerce.infrastructure.event.publisher.EventPublisherImpl;

@Configuration
public class EventConfig {
	@Bean
	public EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		return new EventPublisherImpl(applicationEventPublisher);
	}

}
