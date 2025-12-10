package kr.hhplus.be.commerce.domain.order.model;

import static java.util.Objects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import kr.hhplus.be.commerce.domain.event.Event;
import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.order.event.OrderPlacedEvent;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;
import kr.hhplus.be.commerce.domain.order.model.input.OrderPlaceInput;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record Order(
	Long id,
	Long userId,
	OrderStatus status,
	// 주문가, 주문 라인의 상품 가격 * 주문 수량을 전부 더한 가격
	BigDecimal amount,
	// 할인 가격(쿠폰 등)
	BigDecimal discountAmount,
	// 최종 결제 가격(= amount - discountAmount)
	BigDecimal finalAmount,
	List<OrderLine> orderLines,
	LocalDateTime confirmedAt,
	String idempotencyKey
) {

	public static Order ofPending(Long userId) {
		return Order.builder()
			.userId(userId)
			.status(OrderStatus.PENDING)
			.amount(BigDecimal.ZERO)
			.discountAmount(BigDecimal.ZERO)
			.finalAmount(BigDecimal.ZERO)
			.orderLines(List.of())
			.idempotencyKey("")
			.build();
	}

	public PlaceResult place(OrderPlaceInput input) {
		List<OrderLine> orderLines = input.orderLineInputs()
			.stream()
			.map((orderLineInput) -> OrderLine.place(orderLineInput.productId(), orderLineInput.productName(),
				orderLineInput.productPrice(), orderLineInput.orderQuantity()))
			.toList();

		BigDecimal amount = orderLines.stream()
			.map(OrderLine::totalAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal discountAmount = nonNull(input.discountAmountCalculable()) ?
			input.discountAmountCalculable().calculateDiscountAmount(amount) : BigDecimal.valueOf(0, 2);

		Order order = Order.builder()
			.id(id)
			.userId(input.userId())
			.status(OrderStatus.CONFIRMED)
			.amount(amount)
			.discountAmount(discountAmount)
			.finalAmount(amount.subtract(discountAmount))
			.orderLines(orderLines)
			.confirmedAt(input.now())
			.idempotencyKey(input.idempotencyKey())
			.build();

		return new PlaceResult(order, List.of(OrderPlacedEvent.of(order, input.now())));
	}

	@InfrastructureOnly
	public static Order restore(Long id, Long userId, OrderStatus orderStatus, BigDecimal amount,
		BigDecimal discountAmount, BigDecimal finalAmount, List<OrderLine> orderLines, LocalDateTime confirmedAt,
		String idempotencyKey) {
		return Order.builder()
			.id(id)
			.userId(userId)
			.status(orderStatus)
			.amount(amount)
			.discountAmount(discountAmount)
			.finalAmount(finalAmount)
			.orderLines(orderLines)
			.confirmedAt(confirmedAt)
			.idempotencyKey(idempotencyKey)
			.build();
	}

	public record PlaceResult(
		Order order,
		List<Event> events
	) {

	}
}
