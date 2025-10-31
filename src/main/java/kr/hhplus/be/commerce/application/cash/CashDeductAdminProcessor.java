package kr.hhplus.be.commerce.application.cash;

import java.math.BigDecimal;

import org.springframework.dao.OptimisticLockingFailureException;
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
public class CashDeductAdminProcessor {
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;

	/**
	 * 어드민에 의해서 사용자의 잔액을 차감시키는 프로세서입니다.
	 */
	@Retryable(
		retryFor = OptimisticLockingFailureException.class,
		maxAttempts = 3,
		backoff = @Backoff(delay = 100)
	)
	@Transactional
	public Output execute(Command command) {
		Cash deductedCash = cashRepository.findByUserId(command.userId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_USER))
			.use(command.deductionBalance);

		return new Output(
			cashRepository.save(deductedCash),
			cashHistoryRepository.save(
				CashHistory.recordOfDeduct(command.userId, deductedCash.balance(), command.deductionBalance)
			)
		);
	}

	@Recover
	public Output recover(RuntimeException e, Command command) {
		if (e instanceof OptimisticLockingFailureException) {
			log.error("Exceeded retry count for optimistic lock, userId={}", command.userId, e);
			throw new CommerceException(CommerceCode.EXCEEDED_RETRY_COUNT_FOR_OPTIMISTIC_LOCK);
		}
		throw e;
	}

	public record Command(
		Long userId,
		BigDecimal deductionBalance
	) {
	}

	public record Output(
		Cash cash,
		CashHistory cashHistory
	) {
	}
}
