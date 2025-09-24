package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import java.util.Optional;

import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;

public interface CashRepository {
	Optional<CashEntity> findByUserId(Long userId);

	CashEntity save(CashEntity cash);
}
