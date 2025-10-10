package kr.hhplus.be.commerce.domain.order.repository;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.order.model.Order;

public interface OrderRepository {
	Order save(Order order);
	
	Optional<Order> findByIdempotencyKeyWithLock(String idempotencyKey);

	Optional<Order> findById(Long id);
}
