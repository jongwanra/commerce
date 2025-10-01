package kr.hhplus.be.commerce.domain.order.model;

import java.math.BigDecimal;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderLine(
	Long id,
	Long orderId,
	Long productId,
	String productName,
	BigDecimal productAmount,
	Integer orderQuantity
) {

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

	public BigDecimal totalAmount() {
		return productAmount.multiply(BigDecimal.valueOf(orderQuantity));
	}
}
