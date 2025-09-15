package kr.hhplus.be.commerce.cash.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.commerce.cash.persistence.entity.CashEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CashRepositoryImpl implements CashRepository {
	private final CashJpaRepository cashJpaRepository;

	@Override
	public Optional<CashEntity> findByUserId(Long userId) {
		return cashJpaRepository.findByUserId(userId);
	}

	@Override
	public CashEntity save(CashEntity cash) {
		return cashJpaRepository.save(cash);
	}
}
