package kr.hhplus.be.commerce.application.event.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.commerce.domain.event.OrderConfirmedEvent;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.model.message_payload.OrderConfirmedMessagePayload;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.infrastructure.client.slack.SlackSendMessageClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderConfirmedEventHandler {
	private final SlackSendMessageClient slackSendMessageClient;
	private final MessageRepository messageRepository;
	private final OrderRepository orderRepository;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(OrderConfirmedEvent event) {
		Order order = orderRepository.findById(event.orderId())
			.orElseThrow();

		try {
			slackSendMessageClient.send("...");
		} catch (Exception e) {
			// fallback
			System.out.println("call fallback method! message: " + e.getMessage());
			messageRepository.save(
				Message.ofPending(
					order.id(),
					MessageTargetType.ORDER,
					OrderConfirmedMessagePayload.from(order)
				)
			);

		}
	}
}
