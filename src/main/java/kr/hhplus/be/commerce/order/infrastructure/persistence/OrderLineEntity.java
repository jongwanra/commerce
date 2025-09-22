package kr.hhplus.be.commerce.order.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.global.entity.BaseTimeEntity;
import kr.hhplus.be.commerce.order.domain.model.OrderLine;
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
	private OrderLineEntity(Long orderId, Long productId, String productName,
		BigDecimal productAmount,
		Integer orderQuantity) {
		this.orderId = orderId;
		this.productId = productId;
		this.productName = productName;
		this.productAmount = productAmount;
		this.orderQuantity = orderQuantity;
	}

	public static OrderLineEntity fromDomain(Long orderId, OrderLine orderLine) {
		return OrderLineEntity.builder()
			.orderId(orderId)
			.productId(orderLine.getProductId())
			.productName(orderLine.getProductName())
			.productAmount(orderLine.getProductAmount())
			.orderQuantity(orderLine.getOrderQuantity())
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
