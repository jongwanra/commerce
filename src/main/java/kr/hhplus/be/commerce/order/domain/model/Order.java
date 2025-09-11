package kr.hhplus.be.commerce.order.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import kr.hhplus.be.commerce.order.domain.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Order {
	private Long id;
	private Long userId;
	private OrderStatus status;
	// 주문가, 주문 라인의 상품 가격 * 주문 수량을 전부 더한 가격
	private BigDecimal amount;
	private List<OrderLine> orderLines = new ArrayList<>();

	protected Order() {
	}

	public static Order place(Long userId, List<OrderLine> orderLines) {
		Order order = new Order();
		order.userId = userId;
		order.status = OrderStatus.PENDING;
		order.orderLines = orderLines;
		order.amount = order.calculateTotalAmount();
		return order;
	}

	private BigDecimal calculateTotalAmount() {
		return this.orderLines.stream()
			.map(OrderLine::getTotalAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Builder
	private Order(Long id, Long userId, OrderStatus status, BigDecimal amount, List<OrderLine> orderLines) {
		this.id = id;
		this.userId = userId;
		this.status = status;
		this.amount = amount;
		this.orderLines = orderLines;
	}
}
