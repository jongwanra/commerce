package kr.hhplus.be.commerce.order.domain.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderLine {
	private Long id;
	private Long orderId;
	private Long productId;

	private String productName;
	private BigDecimal productAmount;
	private Integer orderQuantity;

	@Builder
	private OrderLine(Long orderId, Long productId, String productName, BigDecimal productAmount,
		Integer orderQuantity) {
		this.orderId = orderId;
		this.productId = productId;
		this.productName = productName;
		this.productAmount = productAmount;
		this.orderQuantity = orderQuantity;
	}

	public BigDecimal getTotalAmount() {
		return productAmount.multiply(BigDecimal.valueOf(orderQuantity));
	}

	// infrastructure에서만 접근 가능합니다.
	public void assignId(Long id) {
		this.id = id;
	}
}
