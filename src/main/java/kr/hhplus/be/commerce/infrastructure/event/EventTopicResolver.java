package kr.hhplus.be.commerce.infrastructure.event;

import static java.util.Objects.*;

import java.util.Map;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.event.Event;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.event.OrderPlacedEvent;

/**
 * Event의 클래스명에 대응하는 Kafka Topic을 관리하는 클래스입니다.
 */
@Component
public class EventTopicResolver {
	private static final Map<Class<? extends Event>, String> EVENT_TO_TOPIC_MAP = Map.of(
		OrderPlacedEvent.class, "order.placed"
	);

	public String resolve(Event event) {
		final String topic = EVENT_TO_TOPIC_MAP.get(event.getClass());
		
		if (isNull(topic)) {
			throw new CommerceException("No topic mapping found for event: " + event.getClass().getName());
		}
		return topic;

	}
}
