package kr.hhplus.be.commerce.infrastructure.persistence.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderEntity;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT o FROM OrderEntity o WHERE o.id = :orderId")
	Optional<OrderEntity> findByIdWithLock(@Param("orderId") Long orderId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT o FROM OrderEntity o WHERE o.idempotencyKey = :idempotencyKey")
	Optional<OrderEntity> findByIdempotencyKeyWithLock(@Param("idempotencyKey") String idempotencyKey);
}
