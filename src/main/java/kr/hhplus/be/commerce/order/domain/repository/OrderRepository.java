package kr.hhplus.be.commerce.order.domain.repository;

import java.util.Optional;

import kr.hhplus.be.commerce.order.domain.model.Order;

public interface OrderRepository {
	Order save(Order order);

	Optional<Order> findByIdWithLock(Long orderId);
}
