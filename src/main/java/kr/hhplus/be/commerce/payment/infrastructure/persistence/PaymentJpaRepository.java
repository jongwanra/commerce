package kr.hhplus.be.commerce.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.payment.infrastructure.persistence.entity.PaymentEntity;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}
