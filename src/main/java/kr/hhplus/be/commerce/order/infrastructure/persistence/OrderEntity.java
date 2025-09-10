package kr.hhplus.be.commerce.order.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.commerce.global.entity.BaseTimeEntity;
import kr.hhplus.be.commerce.order.domain.model.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// order는 예약어여서 orders로 테이블명 지정
@Table(name = "orders")
public class OrderEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	// 주문가, 주문 라인의 상품 가격 * 주문 수량을 전부 더한 가격
	private BigDecimal amount;
	
}
