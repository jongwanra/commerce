package kr.hhplus.be.commerce.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}
