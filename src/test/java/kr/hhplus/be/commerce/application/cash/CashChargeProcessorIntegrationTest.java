package kr.hhplus.be.commerce.application.cash;

import static kr.hhplus.be.commerce.application.cash.CashChargeProcessor.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.enums.CashHistoryAction;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserJpaRepository;

public class CashChargeProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	private CashChargeProcessor cashChargeProcessor;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private CashRepository cashRepository;

	@Autowired
	private CashHistoryRepository cashHistoryRepository;

	@BeforeEach
	void setUp() {
		cashChargeProcessor = new CashChargeProcessor(cashRepository, cashHistoryRepository);
	}

	// 작성 이유: CashChargeProcessor의 정상 충전 여부를 확인하기 위해 작성했습니다.
	@IntegrationTest
	@Sql(scripts = "/sql/setup_user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	void 잔액이_없는_회원이_1_000원을_충전할_수_있다() {
		// given
		Long userId = userJpaRepository.findByEmail("user.a@gmail.com")
			.orElseThrow(() -> new CommerceException("테스트에 필요한 회원이 존재하지 않습니다. setup.sql을 확인해주세요."))
			.getId();

		BigDecimal amount = BigDecimal.valueOf(1_000);

		Command command = new Command(userId, amount);

		// when
		Output output = cashChargeProcessor.execute(command);

		// then
		assertThat(output.userId()).isEqualTo(userId);
		assertThat(output.originalBalance().compareTo(BigDecimal.ZERO)).isZero();
		assertThat(output.newBalance().compareTo(amount)).isZero();

		List<CashHistoryEntity> cashHistories = cashHistoryRepository.findAllByUserId(userId);
		assertThat(cashHistories).hasSize(1);
		CashHistoryEntity cashHistory = cashHistories.get(0);
		assertThat(cashHistory.getUserId()).isEqualTo(userId);
		assertThat(cashHistory.getAction()).isEqualTo(CashHistoryAction.CHARGE);
		assertThat(cashHistory.getAmount().compareTo(BigDecimal.valueOf(1_000))).isZero().as("충전 금액");
		assertThat(cashHistory.getBalanceAfter().compareTo(BigDecimal.valueOf(1_000))).isZero().as("충전 이후 잔액");
	}
}
