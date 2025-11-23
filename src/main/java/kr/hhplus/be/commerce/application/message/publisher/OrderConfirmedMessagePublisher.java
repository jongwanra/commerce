package kr.hhplus.be.commerce.application.message.publisher;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.OrderConfirmedMessagePayload;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.product_ranking.store.ProductRankingStore;
import kr.hhplus.be.commerce.infrastructure.client.slack.SlackSendMessageClient;

@Component
public class OrderConfirmedMessagePublisher implements MessagePublisher<OrderConfirmedMessagePayload> {
	private final SlackSendMessageClient slackSendMessageClient;
	private final OrderRepository orderRepository;
	private final ProductRankingStore productRankingStore;

	public OrderConfirmedMessagePublisher(SlackSendMessageClient slackSendMessageClient,
		OrderRepository orderRepository, ProductRankingStore productRankingStore) {
		this.slackSendMessageClient = slackSendMessageClient;
		this.orderRepository = orderRepository;
		this.productRankingStore = productRankingStore;
	}

	@Override
	public MessageType getSupportingMessageType() {
		return MessageType.ORDER_CONFIRMED;
	}

	@Override
	public void publish(OrderConfirmedMessagePayload messagePayload) {
		slackSendMessageClient.send(messagePayload.toString());

		Order order = orderRepository.findById(messagePayload.orderId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_ORDER));

		// Redis에 상품 판매량을 증가시킵니다.
		LocalDateTime now = LocalDateTime.now();
		order.orderLines()
			.forEach((orderLine) -> productRankingStore.increment(orderLine.productId(), orderLine.orderQuantity(),
				messagePayload.today(), now));
	}

}
