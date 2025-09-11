package kr.hhplus.be.commerce.product.domain.model;

import java.math.BigDecimal;

import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Product {
	private Long id;
	private String name;
	private Integer stock;
	private BigDecimal price;

	@Builder
	private Product(Long id, String name, Integer stock, BigDecimal price) {
		this.id = id;
		this.name = name;
		this.stock = stock;
		this.price = price;
	}

	public void deductStock(int quantity) {
		if (this.stock < quantity) {
			throw new CommerceException(CommerceCode.INSUFFICIENT_PRODUCT_STOCK);
		}
		this.stock -= quantity;
	}
}
