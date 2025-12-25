package kr.hhplus.be.commerce.application.cash;

import static kr.hhplus.be.commerce.application.cash.CashChargeProcessor.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.cash.model.CashHistory;
import kr.hhplus.be.commerce.domain.cash.model.enums.CashHistoryAction;
import kr.hhplus.be.commerce.domain.cash.repository.CashHistoryRepository;
import kr.hhplus.be.commerce.domain.cash.repository.CashRepository;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;

public class CashChargeProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	@Autowired
	private CashChargeProcessor cashChargeProcessor;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private CashRepository cashRepository;

	@Autowired
	private CashHistoryRepository cashHistoryRepository;

	// 작성 이유: CashChargeProcessor의 정상 충전 여부를 확인하기 위해 작성했습니다.
	@IntegrationTest
	void 잔액이_없는_회원이_1_000원을_충전할_수_있다() {
		// given
		UserEntity user = userJpaRepository.save(UserEntity.builder()
			.email("user@gmail.com")
			.encryptedPassword("encrypted_password")
			.status(UserStatus.ACTIVE)
			.build());
		Long userId = user.getId();
		cashJpaRepository.save(CashEntity.fromDomain(Cash.restore(null, userId, BigDecimal.ZERO, 0L)));

		BigDecimal amount = BigDecimal.valueOf(1_000);

		Command command = new Command(userId, amount);

		// when
		Output output = cashChargeProcessor.execute(command);

		// then
		assertThat(output.userId()).isEqualTo(userId);
		assertThat(output.originalBalance().compareTo(BigDecimal.ZERO)).isZero();
		assertThat(output.newBalance().compareTo(amount)).isZero();

		List<CashHistory> cashHistories = cashHistoryRepository.findAllByUserId(userId);
		assertThat(cashHistories).hasSize(1);
		CashHistory cashHistory = cashHistories.get(0);
		assertThat(cashHistory.userId()).isEqualTo(userId);
		assertThat(cashHistory.action()).isEqualTo(CashHistoryAction.CHARGE);
		assertThat(cashHistory.amount().compareTo(BigDecimal.valueOf(1_000))).isZero().as("충전 금액");
		assertThat(cashHistory.balanceAfter().compareTo(BigDecimal.valueOf(1_000))).isZero().as("충전 이후 잔액");
	}
}
