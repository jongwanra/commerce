package kr.hhplus.be.commerce.application.cash;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashChargeProcessor {
	private final CashJpaRepository cashJpaRepository;
	private final CashHistoryJpaRepository cashHistoryJpaRepository;

	@Transactional
	public Output execute(Command command) {
		CashEntity cash = cashJpaRepository.findByUserId(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		BigDecimal originalBalance = cash.getBalance();
		cash.charge(command.amount);
		BigDecimal newBalance = cash.getBalance();

		cashJpaRepository.save(cash);
		cashHistoryJpaRepository.save(
			CashHistoryEntity.recordOfCharge(
				cash.getUserId(),
				newBalance,
				command.amount()
			)
		);

		return new Output(
			cash.getUserId(),
			originalBalance,
			newBalance
		);
	}

	public record Command(
		Long userId,
		BigDecimal amount
	) {
	}

	public record Output(
		Long userId,
		BigDecimal originalBalance,
		BigDecimal newBalance
	) {
	}
}
