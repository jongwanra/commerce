package kr.hhplus.be.commerce.product.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
	private LocalDateTime createdAt;

	@Builder
	private Product(String name, Integer stock, BigDecimal price, LocalDateTime createdAt) {
		this.name = name;
		this.stock = stock;
		this.price = price;
		this.createdAt = createdAt;
	}

	public void deductStock(int quantity) {
		if (this.stock < quantity) {
			throw new CommerceException(CommerceCode.INSUFFICIENT_PRODUCT_STOCK);
		}
		this.stock -= quantity;
	}

	// infrastructure에서만 접근이 가능합니다.
	public void assignId(Long id) {
		this.id = id;
	}

}
