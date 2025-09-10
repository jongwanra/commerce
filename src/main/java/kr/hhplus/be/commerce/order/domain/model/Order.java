package kr.hhplus.be.commerce.order.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.order.domain.model.enums.OrderStatus;

public class Order {
	private Long id;
	private Long userId;
	private OrderStatus status;
	// 주문가, 주문 라인의 상품 가격 * 주문 수량을 전부 더한 가격
	private BigDecimal amount;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
