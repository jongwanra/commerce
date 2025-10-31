package kr.hhplus.be.commerce.application.cash;

import static kr.hhplus.be.commerce.application.cash.CashChargeProcessor.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.cash.model.CashHistory;
import kr.hhplus.be.commerce.domain.cash.repository.CashHistoryRepository;
import kr.hhplus.be.commerce.domain.cash.repository.CashRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.global.AbstractUnitTestSupport;

@ExtendWith(MockitoExtension.class)
class CashChargeProcessorUnitTest extends AbstractUnitTestSupport {
	@InjectMocks
	private CashChargeProcessor cashChargeProcessor;
	@Mock
	private CashRepository cashRepository;
	@Mock
	private CashHistoryRepository cashHistoryRepository;

	// 작성 이유: 잔액 충전의 일반적인 케이스를 검증하기 위해서 작성했습니다.[경계값 검증]
	@Test
	void 잔액_1_000원을_가진_사용자가_1원을_충전한다면_이후_잔액은_1_001원이다() {
		// given
		Long userId = 1L;
		BigDecimal currentBalance = BigDecimal.valueOf(1_000);
		BigDecimal chargeAmount = BigDecimal.valueOf(1);

		Cash cash = Cash.restore(
			1L,
			userId,
			currentBalance,
			0L
		);

		// mock
		given(cashRepository.findByUserId(userId))
			.willReturn(Optional.of(cash));

		// when
		Output output = cashChargeProcessor.execute(new Command(
			userId,
			chargeAmount
		));

		// then
		verify(cashRepository, times(1)).findByUserId(userId);
		verify(cashRepository, times(1)).save(any(Cash.class));
		verify(cashHistoryRepository, times(1)).save(any(CashHistory.class));

		assertThat(output.originalBalance()).isEqualTo(BigDecimal.valueOf(1_000));
		assertThat(output.newBalance()).isEqualTo(BigDecimal.valueOf(1_001));
	}

	// 작성 이유: 충전할 금액이 0원일 경우 예외가 발생하는지 검증하기 위해서 작성했습니다.[경계값 검증]
	@Test
	void 충전_금액이_0원일_경우_예외를_발생시킨다() {
		// given
		Long userId = 1L;
		BigDecimal currentBalance = BigDecimal.valueOf(1_000);
		BigDecimal chargeAmount = BigDecimal.ZERO;

		Cash cash = Cash.restore(1L, userId, currentBalance, 0L);

		// mock
		given(cashRepository.findByUserId(userId))
			.willReturn(Optional.of(cash));

		// when
		assertThatThrownBy(() -> {
			cashChargeProcessor.execute(new Command(
				userId,
				chargeAmount
			));
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("금액은 0원보다 커야 합니다.");

		// then
		verify(cashRepository, times(1)).findByUserId(userId);
		verify(cashRepository, never()).save(any(Cash.class));
		verify(cashHistoryRepository, never()).save(any(CashHistory.class));
	}

	// 작성 이유: 한 번에 1000만원 까지 충전 가능함을 검증하기 위해 작성했습니다. [경계값 검증]
	@Test
	void 사용자는_한_번에_1000만원을_충전할_수_있다() {
		// given
		Long userId = 1L;
		BigDecimal currentBalance = BigDecimal.valueOf(1_000);
		BigDecimal chargeAmount = BigDecimal.valueOf(10_000_000);

		Cash cash = Cash.restore(1L, userId, currentBalance, 0L);

		// mock
		given(cashRepository.findByUserId(userId))
			.willReturn(Optional.of(cash));

		// when
		Output output = cashChargeProcessor.execute(new Command(
			userId,
			chargeAmount
		));

		// then
		verify(cashRepository, times(1)).findByUserId(userId);
		verify(cashRepository, times(1)).save(any(Cash.class));
		verify(cashHistoryRepository, times(1)).save(any(CashHistory.class));

		assertThat(output.originalBalance()).isEqualTo(BigDecimal.valueOf(1_000));
		assertThat(output.newBalance()).isEqualTo(BigDecimal.valueOf(10_001_000));
	}

	// 작성 이유: 충전할 금액이 1000만원 초과일 경우 예외가 발생하는지 검증하기 위해서 작성했습니다.[경계값 검증]
	@Test
	void 한_번에_충전할_수_있는_금액인_1000만원을_초과할_경우_예외를_발생_시킨다() {
		// given
		Long userId = 1L;
		BigDecimal currentBalance = BigDecimal.valueOf(1_000);
		BigDecimal chargeAmount = BigDecimal.valueOf(10_000_001);

		Cash cash = Cash.restore(1L, userId, currentBalance, 0L);

		// mock
		given(cashRepository.findByUserId(userId))
			.willReturn(Optional.of(cash));

		// when
		assertThatThrownBy(() -> {
			cashChargeProcessor.execute(new Command(
				userId,
				chargeAmount
			));
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("한 번에 10,000,000원을 초과하여 충전할 수 없습니다.");

		// then
		verify(cashRepository, times(1)).findByUserId(userId);
		verify(cashRepository, never()).save(any(Cash.class));
		verify(cashHistoryRepository, never()).save(any(CashHistory.class));
	}

}
