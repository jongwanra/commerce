package kr.hhplus.be.commerce.order.infrastructure.persistence;

import static java.util.Objects.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.order.domain.model.Order;
import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;
import kr.hhplus.be.commerce.order.infrastructure.persistence.entity.OrderEntity;
import kr.hhplus.be.commerce.order.infrastructure.persistence.entity.OrderLineEntity;
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
