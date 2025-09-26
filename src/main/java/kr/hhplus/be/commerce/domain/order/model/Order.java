package kr.hhplus.be.commerce.domain.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;
import kr.hhplus.be.commerce.domain.order.model.input.OrderPlaceInput;
import kr.hhplus.be.commerce.domain.order.policy.DiscountAmountCalculable;
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
	LocalDateTime confirmedAt
) {

	public static Order place(OrderPlaceInput input) {
		List<OrderLine> orderLines = input.orderLineInputs()
			.stream()
			.map((orderLineInput) -> OrderLine.place(orderLineInput.productId(), orderLineInput.productName(),
				orderLineInput.productPrice(), orderLineInput.orderQuantity()))
			.toList();

		BigDecimal amount = orderLines.stream()
			.map(OrderLine::getTotalAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		return Order.builder()
			.userId(input.userId())
			.status(OrderStatus.PENDING)
			.amount(amount)
			.discountAmount(BigDecimal.ZERO)
			.finalAmount(BigDecimal.ZERO)
			.orderLines(orderLines)
			.confirmedAt(null)
			.build();
	}

	@InfrastructureOnly
	public static Order restore(Long id, Long userId, OrderStatus orderStatus, BigDecimal amount,
		BigDecimal discountAmount, BigDecimal finalAmount, List<OrderLine> orderLines, LocalDateTime confirmedAt) {
		return Order.builder()
			.id(id)
			.userId(userId)
			.status(orderStatus)
			.amount(amount)
			.discountAmount(discountAmount)
			.finalAmount(finalAmount)
			.orderLines(orderLines)
			.confirmedAt(confirmedAt)
			.build();
	}

	public void authorize(Long userId) {
		if (this.userId.equals(userId)) {
			return;
		}
		throw new CommerceException(CommerceCode.UNAUTHORIZED_USER);
	}

	public Order confirm(LocalDateTime now, DiscountAmountCalculable discountAmountCalculable) {
		if (status.isConfirmed()) {
			throw new CommerceException(CommerceCode.ALREADY_CONFIRMED_ORDER);
		}

		BigDecimal discountAmount = discountAmountCalculable.calculateDiscountAmount(this.amount);
		return Order.builder()
			.id(this.id)
			.userId(this.userId)
			.status(OrderStatus.CONFIRMED)
			.amount(this.amount)
			.discountAmount(discountAmount)
			.finalAmount(this.amount.subtract(discountAmount))
			.orderLines(this.orderLines)
			.confirmedAt(now)
			.build();

	}

	public Order confirm(LocalDateTime now) {
		if (status.isConfirmed()) {
			throw new CommerceException(CommerceCode.ALREADY_CONFIRMED_ORDER);
		}
		BigDecimal discountAmount = BigDecimal.valueOf(0, 2);
		return Order.builder()
			.id(this.id)
			.userId(this.userId)
			.status(OrderStatus.CONFIRMED)
			.amount(this.amount)
			.discountAmount(discountAmount)
			.finalAmount(this.amount.subtract(this.discountAmount))
			.orderLines(this.orderLines)
			.confirmedAt(now)
			.build();
	}

}
