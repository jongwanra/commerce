package kr.hhplus.be.commerce.order.infrastructure.persistence;

import static java.util.Objects.*;

import java.util.List;
import java.util.Optional;

import kr.hhplus.be.commerce.order.domain.model.Order;
import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
	private final OrderJpaRepository orderJpaRepository;
	private final OrderLineJpaRepository orderLineJpaRepository;

	@Override
	public Order save(Order order) {
		OrderEntity orderEntity = orderJpaRepository.save(OrderEntity.fromDomain(order));
		List<OrderLineEntity> orderLineEntities = orderLineJpaRepository.saveAll(order.getOrderLines()
			.stream()
			.map(orderLine -> OrderLineEntity.fromDomain(orderEntity.getId(), orderLine))
			.toList()
		);

		return orderEntity.toDomain(orderLineEntities);

	}

	@Override
	public Optional<Order> findByIdWithLock(Long orderId) {
		if (isNull(orderId)) {
			return Optional.empty();
		}
		return orderJpaRepository.findByIdWithLock(orderId)
			.map((orderEntity) -> {
				List<OrderLineEntity> orderLineEntities = orderLineJpaRepository.findAllByOrderId(orderEntity.getId());
				return orderEntity.toDomain(orderLineEntities);
			});
	}

}
