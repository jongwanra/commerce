package kr.hhplus.be.commerce.cash.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hhplus.be.commerce.cash.persistence.entity.CashHistoryEntity;

public interface CashHistoryJpaRepository extends JpaRepository<CashHistoryEntity, Long> {
}
