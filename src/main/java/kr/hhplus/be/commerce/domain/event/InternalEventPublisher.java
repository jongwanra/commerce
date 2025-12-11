package kr.hhplus.be.commerce.domain.event;

import java.util.Collection;

public interface InternalEventPublisher {
	void publish(Collection<? extends Event> events);

	void publish(Event event);
}
