package kr.hhplus.be.commerce.domain.order.event;

import java.time.LocalDateTime;
import java.util.List;

import kr.hhplus.be.commerce.domain.event.Event;
import kr.hhplus.be.commerce.domain.order.model.Order;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderPlacedEvent(
	LocalDateTime occurredAt,
	Long orderId,
	List<OrderLineSummary> orderLines
) implements Event {
	public static Event of(Order order) {
		return OrderPlacedEvent
			.builder()
			.occurredAt(LocalDateTime.now())
			.orderId(order.id())
			.orderLines(order.orderLines().stream()
				.map((orderLine) -> new OrderLineSummary(orderLine.productId(), orderLine.orderQuantity()))
				.toList()
			)
			.build();
	}

	public record OrderLineSummary(
		Long productId,
		Integer orderQuantity
	) {
	}
}
