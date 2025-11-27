package kr.hhplus.be.commerce.infrastructure.event;

import java.util.Collection;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.event.Event;
import kr.hhplus.be.commerce.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {
	@Override
	public void publish(Collection<? extends Event> events) {
		events.forEach(applicationEventPublisher::publishEvent);
	}

	private final ApplicationEventPublisher applicationEventPublisher;
	
	@Override
	public void publish(Event event) {
		applicationEventPublisher.publishEvent(event);
	}
}
