package kr.hhplus.be.commerce.order.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.order.domain.repository.OrderRepository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
	private final OrderJpaRepository orderJpaRepository;

	public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository) {
		this.orderJpaRepository = orderJpaRepository;
	}

}
