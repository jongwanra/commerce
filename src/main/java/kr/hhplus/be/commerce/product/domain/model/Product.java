package kr.hhplus.be.commerce.product.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PUBLIC)
public class Product {
	private Long id;
	private String name;
	private Integer stock;
	private BigDecimal price;
	private LocalDateTime createdAt;

	public void deductStock(int quantity) {
		if (this.stock < quantity) {
			throw new CommerceException(CommerceCode.INSUFFICIENT_PRODUCT_STOCK);
		}
		this.stock -= quantity;
	}
}
