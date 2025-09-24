package kr.hhplus.be.commerce.infrastructure.persistence.order;

import static java.util.Objects.*;

import java.util.List;
import java.util.Optional;

import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderLineEntity;
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
