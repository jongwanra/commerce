package kr.hhplus.be.commerce.infrastructure.persistence.order.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.infrastructure.persistence.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_line")
@Getter
public class OrderLineEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long orderId;
	private Long productId;

	@Column(name = "product_name_snapshot", updatable = false)
	private String productName;
	@Column(name = "product_amount_snapshot", updatable = false)
	private BigDecimal productAmount;

	private Integer orderQuantity;

	@Builder
	private OrderLineEntity(Long id, Long orderId, Long productId, String productName,
		BigDecimal productAmount,
		Integer orderQuantity) {
		this.id = id;
		this.orderId = orderId;
		this.productId = productId;
		this.productName = productName;
		this.productAmount = productAmount;
		this.orderQuantity = orderQuantity;
	}

	public static OrderLineEntity fromDomain(Long orderId, OrderLine orderLine) {
		return OrderLineEntity.builder()
			.id(orderLine.id())
			.orderId(orderId)
			.productId(orderLine.productId())
			.productName(orderLine.productName())
			.productAmount(orderLine.productAmount())
			.orderQuantity(orderLine.orderQuantity())
			.build();
	}

	public OrderLine toDomain() {
		return OrderLine.restore(
			id,
			orderId,
			productId,
			productName,
			productAmount,
			orderQuantity
		);
	}
}
