package kr.hhplus.be.commerce.infrastructure.persistence.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.infrastructure.persistence.payment.entity.PaymentEntity;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}
