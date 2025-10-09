package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import java.util.Optional;

import kr.hhplus.be.commerce.domain.cash.model.Cash;

public interface CashRepository {
	Optional<Cash> findByUserId(Long userId);

	Cash save(Cash cash);
}
