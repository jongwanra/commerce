package kr.hhplus.be.commerce.infrastructure.persistence.product.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
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

	@Builder
	private ProductEntity(Long id, String name, Integer stock, BigDecimal price) {
		this.id = id;
		this.name = name;
		this.stock = stock;
		this.price = price;
	}

	public static ProductEntity fromDomain(Product product) {
		return ProductEntity.builder()
			.id(product.id())
			.name(product.name())
			.stock(product.stock())
			.price(product.price())
			.build();
	}

	public Product toDomain() {
		return Product.restore(
			id,
			name,
			stock,
			price,
			getCreatedAt()
		);
	}

}
