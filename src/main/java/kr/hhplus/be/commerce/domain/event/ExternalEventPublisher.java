package kr.hhplus.be.commerce.domain.event;

/**
 * 외부로 이벤트를 발행하는 역할을합니다.
 */
public interface ExternalEventPublisher {
	void publish(Event event);
}
