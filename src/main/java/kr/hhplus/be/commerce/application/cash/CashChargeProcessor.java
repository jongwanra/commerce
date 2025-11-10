package kr.hhplus.be.commerce.application.cash;

import java.math.BigDecimal;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.cash.model.CashHistory;
import kr.hhplus.be.commerce.domain.cash.repository.CashHistoryRepository;
import kr.hhplus.be.commerce.domain.cash.repository.CashRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashChargeProcessor {
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;

	@Retryable(
		retryFor = {
			// 낙관적 락 충돌 시 발생합니다.
			OptimisticLockingFailureException.class,
			// 비관적 락 획득 실패 시 발생합니다.
			PessimisticLockingFailureException.class
		},
		maxAttempts = 3,
		backoff = @Backoff(delay = 100)
	)
	@Transactional
	public Output execute(Command command) {
		Cash originalCash = cashRepository.findByUserId(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		Cash chargedCash = originalCash.charge(command.amount);

		cashRepository.save(chargedCash);
		cashHistoryRepository.save(
			CashHistory.recordOfCharge(
				chargedCash.userId(),
				chargedCash.balance(),
				command.amount()
			)
		);

		return new Output(
			chargedCash.userId(),
			originalCash.balance(),
			chargedCash.balance()
		);
	}

	@Recover
	public Output recover(RuntimeException e, Command command) {
		if (e instanceof OptimisticLockingFailureException) {
			log.error("Exceeded retry count for optimistic lock, command={}", command, e);
			throw new CommerceException(CommerceCode.EXCEEDED_RETRY_COUNT_FOR_LOCK);
		}
		if (e instanceof PessimisticLockingFailureException) {
			log.error("Exceeded retry count for pessimistic lock, command={}", command, e);
			throw new CommerceException(CommerceCode.EXCEEDED_RETRY_COUNT_FOR_LOCK);
		}
		throw e;
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
