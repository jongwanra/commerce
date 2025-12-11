package kr.hhplus.be.commerce.application.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.event.ExternalEventPublisher;
import kr.hhplus.be.commerce.domain.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;

/**
 * {@link OrderPlaceProcessor}의 후처리 로직입니다.
 * 트랜잭션 커밋 이후에 동기적으로 이벤트를 처리합니다.
 *
 * Kafka를 통해 메세지를 발행하면 아래의 EventListener들이 메세지를 읽습니다.
 * @see OrderPlacedNotificationEventListener
 * @see OrderPlacedProductRankingEventListener
 *
 */
@Component
@RequiredArgsConstructor
public class OrderPlacedEventListener {
	private final ExternalEventPublisher externalEventPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(OrderPlacedEvent event) {
		externalEventPublisher.publish(event);

	}
}
