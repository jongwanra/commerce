package kr.hhplus.be.commerce.cash.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashHistoryJpaRepository extends JpaRepository<CashHistoryEntity, Long> {
}
