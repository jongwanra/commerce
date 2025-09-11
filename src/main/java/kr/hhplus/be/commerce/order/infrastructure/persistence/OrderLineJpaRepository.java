package kr.hhplus.be.commerce.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineJpaRepository extends JpaRepository<OrderLineEntity, Long> {
}
