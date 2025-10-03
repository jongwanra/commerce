package kr.hhplus.be.commerce.infrastructure.event.publisher;

import org.springframework.context.ApplicationEventPublisher;

import kr.hhplus.be.commerce.application.event.publisher.EventPublisher;
import kr.hhplus.be.commerce.domain.event.Event;

public class EventPublisherImpl implements EventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;

	public EventPublisherImpl(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void publish(Event event) {
		applicationEventPublisher.publishEvent(event);
	}
}
