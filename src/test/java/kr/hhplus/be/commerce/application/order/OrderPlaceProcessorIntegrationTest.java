package kr.hhplus.be.commerce.application.order;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import kr.hhplus.be.commerce.application.cash.CashChargeProcessor;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.ScenarioIntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.enums.CashHistoryAction;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserJpaRepository;

class OrderPlaceProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	private CashChargeProcessor cashChargeProcessor;
	private OrderPlaceProcessor orderPlaceProcessor;

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private CashJpaRepository cashJpaRepository;

	@Autowired
	private CashHistoryJpaRepository cashHistoryJpaRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		cashChargeProcessor = new CashChargeProcessor(cashJpaRepository, cashHistoryJpaRepository);
		orderPlaceProcessor = new OrderPlaceProcessor(orderRepository, productRepository);
	}

	@Sql(scripts = {"/sql/setup_user.sql",
		"/sql/setup_product.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@ScenarioIntegrationTest
	Stream<DynamicTest> 잔액을_충전하고_주문을_할_수_있다() {
		Long userId = userJpaRepository.findByEmail("user.a@gmail.com")
			.orElseThrow(() -> new CommerceException("테스트에 필요한 회원이 존재하지 않습니다. setup_user.sql을 확인해주세요."))
			.getId();

		return Stream.of(
			DynamicTest.dynamicTest("잔액 10,000원을 충전한다.", () -> {
				// given
				BigDecimal amount = BigDecimal.valueOf(10_000);

				CashChargeProcessor.Command command = new CashChargeProcessor.Command(userId, amount);

				// when
				CashChargeProcessor.Output output = transactionTemplate.execute(
					(status -> cashChargeProcessor.execute(command)));

				// then
				assertThat(output.userId()).isEqualTo(userId);
				assertThat(output.originalBalance().compareTo(BigDecimal.ZERO)).isZero();
				assertThat(output.newBalance().compareTo(BigDecimal.valueOf(10_000))).isZero().as("잔액 1만원 충전");

				List<CashHistoryEntity> cashHistories = cashHistoryJpaRepository.findAllByUserId(userId);
				assertThat(cashHistories).hasSize(1);
				CashHistoryEntity cashHistory = cashHistories.get(0);
				assertThat(cashHistory.getUserId()).isEqualTo(userId);
				assertThat(cashHistory.getAction()).isEqualTo(CashHistoryAction.CHARGE);
				assertThat(cashHistory.getAmount().compareTo(BigDecimal.valueOf(10_000))).isZero().as("충전 금액");
				assertThat(cashHistory.getBalanceAfter().compareTo(BigDecimal.valueOf(10_000))).isZero().as("충전 이후 잔액");
			}),
			DynamicTest.dynamicTest("주문을 한다", () -> {
				// given
				Product product = productRepository.findByName("오뚜기 진라면 매운맛 120g")
					.orElseThrow(() -> new CommerceException("테스트에 필요한 상품이 존재하지 않습니다. setup_product.sql을 확인해주세요."));

				// when
				OrderPlaceProcessor.Output output = transactionTemplate.execute(
					(status) -> orderPlaceProcessor.execute(new OrderPlaceProcessor.Command(
						userId,
						List.of(
							new OrderPlaceProcessor.OrderLineCommand(product.id(), 1)
						)
					)));

				// then
				Order order = output.order();
				assertThat(order.getUserId()).isEqualTo(userId);
				assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
				assertThat(order.getAmount().compareTo(BigDecimal.valueOf(6_700))).isZero();
				assertThat(order.getOrderLines()).hasSize(1);

				OrderLine orderLine = order.getOrderLines().get(0);
				assertThat(orderLine.getProductId()).isEqualTo(product.id());
				assertThat(orderLine.getProductName()).isEqualTo(product.name());
				assertThat(orderLine.getOrderQuantity()).isOne();
				assertThat(orderLine.getTotalAmount().compareTo(product.price())).isZero();

				assertThat(output.products().size()).isOne();
				Product productOfOutput = output.products().get(0);
				assertThat(productOfOutput.id()).isEqualTo(product.id());
				assertThat(productOfOutput.stock()).isEqualTo(99).as("100개 중 1개 주문하여 재고가 99개 남아야 합니다.");

				CashEntity cash = cashJpaRepository.findByUserId(userId)
					.orElseThrow(() -> new CommerceException("테스트에 필요한 회원의 잔액 정보가 존재하지 않습니다. setup_user.sql을 확인해주세요."));

				assertThat(cash.getBalance().compareTo(BigDecimal.valueOf(10_000))).isZero()
					.as("잔액은 10,000원을 그대로 유지합니다. 잔액 차감은 주문 시점이 아니라, 결제 시점에 진행됩니다.");
			})
		);
	}
}
