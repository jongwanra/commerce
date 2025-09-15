package kr.hhplus.be.commerce.cash.persistence;

import java.util.Optional;

import kr.hhplus.be.commerce.cash.persistence.entity.CashEntity;

public interface CashRepository {
	Optional<CashEntity> findByUserId(Long userId);

	CashEntity save(CashEntity cash);
}
