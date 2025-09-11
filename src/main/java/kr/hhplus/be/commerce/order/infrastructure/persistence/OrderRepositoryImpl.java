package kr.hhplus.be.commerce.order.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.order.domain.model.Order;
import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
	private final OrderJpaRepository orderJpaRepository;
	private final OrderLineJpaRepository orderLineJpaRepository;

	@Override
	public Order save(Order order) {
		OrderEntity orderEntity = orderJpaRepository.save(OrderEntity.fromDomain(order));
		Long orderId = orderEntity.getId();
		List<OrderLineEntity> orderLineEntities = orderLineJpaRepository.saveAll(
			order.getOrderLines()
				.stream()
				.map(orderLine -> {
					// TODO orderId를 생성 후에 할당하는 부분이 조금 어색하다. 다른 방법 없을까?
					orderLine.assignOrderId(orderId);
					return orderLine;
				})
				.map(OrderLineEntity::fromDomain)
				.toList());

		return orderEntity.toDomain(orderLineEntities);
	}
}
