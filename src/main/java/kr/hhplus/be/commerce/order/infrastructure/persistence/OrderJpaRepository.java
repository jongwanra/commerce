package kr.hhplus.be.commerce.order.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT o FROM OrderEntity o WHERE o.id = :orderId")
	Optional<OrderEntity> findByIdWithLock(@Param("orderId") Long orderId);
}
