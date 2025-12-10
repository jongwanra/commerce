package kr.hhplus.be.commerce.application.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.order.event.OrderPlacedEvent;
import kr.hhplus.be.commerce.infrastructure.client.slack.SlackSendMessageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link OrderPlaceProcessor}의 후처리 로직입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedNotificationEventListener {
	private final SlackSendMessageClient slackSendMessageClient;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(OrderPlacedEvent event) {
		try {
			log.debug("[+OrderPlacedNotificationEventListener] 진입: Thread={}", Thread.currentThread().getName());
			final String message = new StringBuilder()
				.append("[주문 확정🎉]")
				.append(" orderId=" + event.orderId())
				.append(" 주문 확정 일시=" + event.occurredAt())
				.toString();

			slackSendMessageClient.send(message);
		} catch (Exception e) {
			log.error("[알수 없는 에러 발생] 주문 확정 이후, 슬랙 메세지를 보내는데 에러가 발생했습니다.", e);
		}
	}

}
