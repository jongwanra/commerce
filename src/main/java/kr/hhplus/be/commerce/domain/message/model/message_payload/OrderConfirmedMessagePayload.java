package kr.hhplus.be.commerce.domain.message.model.message_payload;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderConfirmedMessagePayload(
	MessageType type,
	Long orderId,
	LocalDate today,
	LocalDateTime now
) implements MessagePayload {
	public static OrderConfirmedMessagePayload from(Long orderId, LocalDate today, LocalDateTime now) {
		return OrderConfirmedMessagePayload.builder()
			.type(MessageType.ORDER_CONFIRMED)
			.orderId(orderId)
			.today(today)
			.now(now)
			.build();
	}

}
