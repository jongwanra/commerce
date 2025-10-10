package kr.hhplus.be.commerce.infrastructure.persistence.cash;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CashRepositoryImpl implements CashRepository {
	private final CashJpaRepository cashJpaRepository;

	@Override
	public Optional<Cash> findByUserId(Long userId) {
		return cashJpaRepository.findByUserId(userId)
			.map(CashEntity::toDomain);
	}

	@Override
	public Cash save(Cash cash) {
		return cashJpaRepository.save(CashEntity.fromDomain(cash))
			.toDomain();
	}
}
