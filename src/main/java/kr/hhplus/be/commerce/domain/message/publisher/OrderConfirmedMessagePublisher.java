package kr.hhplus.be.commerce.domain.message.publisher;

import org.springframework.stereotype.Component;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.model.message_payload.MessagePayload;
import kr.hhplus.be.commerce.infrastructure.client.slack.SlackSendMessageClient;

@Component
public class OrderConfirmedMessagePublisher implements MessagePublisher {
	private final SlackSendMessageClient slackSendMessageClient;

	public OrderConfirmedMessagePublisher(SlackSendMessageClient slackSendMessageClient) {
		this.slackSendMessageClient = slackSendMessageClient;
	}

	@Override
	public MessageType getSupportingMessageType() {
		return MessageType.ORDER_CONFIRMED;
	}

	@Override
	public void publish(MessagePayload messagePayload) {
		slackSendMessageClient.send(messagePayload.toString());
	}
}
