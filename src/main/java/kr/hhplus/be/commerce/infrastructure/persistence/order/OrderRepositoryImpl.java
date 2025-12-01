package kr.hhplus.be.commerce.infrastructure.persistence.order;

import static java.util.stream.Collectors.*;
import static kr.hhplus.be.commerce.infrastructure.persistence.order.entity.QOrderEntity.*;
import static kr.hhplus.be.commerce.infrastructure.persistence.order.entity.QOrderLineEntity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderLineEntity;

public class OrderRepositoryImpl implements OrderRepository {
	private final OrderJpaRepository orderJpaRepository;
	private final OrderLineJpaRepository orderLineJpaRepository;
	private final JPAQueryFactory queryFactory;

	public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, OrderLineJpaRepository orderLineJpaRepository,
		EntityManager entityManager) {
		this.orderJpaRepository = orderJpaRepository;
		this.orderLineJpaRepository = orderLineJpaRepository;
		this.queryFactory = new JPAQueryFactory(entityManager);
	}

	@Override
	public Order save(Order order) {
		OrderEntity orderEntity = orderJpaRepository.save(OrderEntity.fromDomain(order));
		List<OrderLineEntity> orderLineEntities = orderLineJpaRepository.saveAll(order.orderLines()
			.stream()
			.map(orderLine -> OrderLineEntity.fromDomain(orderEntity.getId(), orderLine))
			.toList()
		);

		return orderEntity.toDomain(orderLineEntities);

	}

	@Override
	public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
		if (idempotencyKey.isBlank()) {
			return Optional.empty();
		}
		return orderJpaRepository.findByIdempotencyKey(idempotencyKey)
			.map((orderEntity) -> {
				List<OrderLineEntity> orderLineEntities = orderLineJpaRepository.findAllByOrderId(orderEntity.getId());
				return orderEntity.toDomain(orderLineEntities);
			});

	}

	@Override
	public List<Order> findAllDailyConfirmed(LocalDate date) {
		List<OrderEntity> orderEntities = queryFactory
			.selectFrom(orderEntity)
			.where(
				orderEntity.status.eq(OrderStatus.CONFIRMED),
				orderEntity.confirmedAt.between(date.atStartOfDay(), date.plusDays(1).atStartOfDay())
			)
			.fetch();

		List<Long> orderIds = orderEntities.stream().map(OrderEntity::getId).toList();

		Map<Long, List<OrderLineEntity>> orderIdToOrderLinesMap = queryFactory.selectFrom(orderLineEntity)
			.where(orderLineEntity.orderId.in(orderIds))
			.fetch()
			.stream()
			.collect(groupingBy(OrderLineEntity::getOrderId, mapping(it -> it, toList())));

		return orderEntities.stream()
			.map(orderEntity -> {
				List<OrderLineEntity> orderLineEntities = orderIdToOrderLinesMap.getOrDefault(orderEntity.getId(),
					List.of());

				return orderEntity.toDomain(orderLineEntities);
			})
			.toList();
	}

	@Override
	public Optional<Order> findById(Long orderId) {
		return orderJpaRepository.findById(orderId)
			.map((orderEntity) -> {
				List<OrderLineEntity> orderLineEntities = orderLineJpaRepository.findAllByOrderId(orderEntity.getId());
				return orderEntity.toDomain(orderLineEntities);
			});
	}

}
