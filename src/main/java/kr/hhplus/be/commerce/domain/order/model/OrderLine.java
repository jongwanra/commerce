package kr.hhplus.be.commerce.domain.order.model;

import static java.util.Objects.*;

import java.math.BigDecimal;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class OrderLine {
	private final Long id;
	private final Long orderId;
	private final Long productId;
	private final String productName;
	private final BigDecimal productAmount;
	private final Integer orderQuantity;

	@Builder(access = AccessLevel.PRIVATE)
	private OrderLine(Long id, Long orderId, Long productId, String productName, BigDecimal productAmount,
		Integer orderQuantity) {
		this.id = isNull(id) ? 0L : id;
		this.orderId = orderId;
		this.productId = productId;
		this.productName = productName;
		this.productAmount = productAmount;
		this.orderQuantity = orderQuantity;
	}

	public static OrderLine place(Long productId, String productName, BigDecimal productAmount, Integer orderQuantity) {
		return OrderLine.builder()
			.productId(productId)
			.productName(productName)
			.productAmount(productAmount)
			.orderQuantity(orderQuantity)
			.build();
	}

	@InfrastructureOnly
	public static OrderLine restore(Long id, Long orderId, Long productId, String productName, BigDecimal productAmount,
		Integer orderQuantity) {
		return OrderLine.builder()
			.id(id)
			.orderId(orderId)
			.productId(productId)
			.productName(productName)
			.productAmount(productAmount)
			.orderQuantity(orderQuantity)
			.build();
	}

	public BigDecimal getTotalAmount() {
		return productAmount.multiply(BigDecimal.valueOf(orderQuantity));
	}
}
