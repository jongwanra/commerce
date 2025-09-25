package kr.hhplus.be.commerce.domain.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.global.annotation.InfrastructureOnly;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record Product(
	Long id,
	String name,
	Integer stock,
	BigDecimal price,
	LocalDateTime createdAt
) {

	@InfrastructureOnly
	public static Product restore(Long id, String name, Integer stock, BigDecimal price, LocalDateTime createdAt) {
		return Product.builder()
			.id(id)
			.name(name)
			.stock(stock)
			.price(price)
			.createdAt(createdAt)
			.build();
	}

	public Product deductStock(int quantity) {
		if (this.stock < quantity) {
			throw new CommerceException(CommerceCode.INSUFFICIENT_PRODUCT_STOCK);
		}

		return Product.builder()
			.id(this.id)
			.name(this.name)
			.stock(this.stock - quantity)
			.price(this.price)
			.createdAt(this.createdAt)
			.build();
	}

}
