package kr.hhplus.be.commerce.order.domain.repository;

import kr.hhplus.be.commerce.order.domain.model.Order;

public interface OrderRepository {
	Order save(Order order);
}
