package kr.hhplus.be.commerce.cash.persistence;

import java.util.Optional;

public interface CashRepository {
	Optional<CashEntity> findByUserId(Long userId);

	CashEntity save(CashEntity cash);
}
