package kr.hhplus.be.commerce.product.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.global.entity.BaseTimeEntity;
import kr.hhplus.be.commerce.product.domain.model.Product;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
@Getter
public class ProductEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private Integer stock;
	private BigDecimal price;

	public Product toDomain() {
		return Product.builder()
			.id(id)
			.name(name)
			.stock(stock)
			.price(price)
			.build();
	}

	public static ProductEntity fromDomain(Product product) {
		ProductEntity entity = new ProductEntity();
		entity.id = product.getId();
		entity.name = product.getName();
		entity.stock = product.getStock();
		entity.price = product.getPrice();
		return entity;
	}
}
