package kr.hhplus.be.commerce.cash.application;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.cash.persistence.CashEntity;
import kr.hhplus.be.commerce.cash.persistence.CashHistoryEntity;
import kr.hhplus.be.commerce.cash.persistence.CashHistoryJpaRepository;
import kr.hhplus.be.commerce.cash.persistence.CashJpaRepository;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
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
				originalBalance
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
