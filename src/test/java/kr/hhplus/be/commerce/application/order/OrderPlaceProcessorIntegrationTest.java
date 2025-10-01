package kr.hhplus.be.commerce.application.order;

import static kr.hhplus.be.commerce.application.order.OrderPlaceProcessor.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import kr.hhplus.be.commerce.application.cash.CashChargeProcessor;
import kr.hhplus.be.commerce.domain.message.enums.MessageStatus;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.enums.MessageType;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.ScenarioIntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.enums.CashHistoryAction;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.message.entity.MessageEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.product.entity.ProductEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;

class OrderPlaceProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	private CashChargeProcessor cashChargeProcessor;
	private OrderPlaceProcessor orderPlaceProcessor;

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private UserCouponRepository userCouponRepository;

	@Autowired
	private CashRepository cashRepository;

	@Autowired
	private CashHistoryRepository cashHistoryRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private MessageRepository messageRepository;

	@BeforeEach
	void setUp() {
		cashChargeProcessor = new CashChargeProcessor(cashRepository, cashHistoryRepository);
		orderPlaceProcessor = new OrderPlaceProcessor(orderRepository,
			paymentRepository,
			productRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository,
			messageRepository);
	}

	/**
	 * 작성 이유: 상품을 주문하고 결제를 하는 전체 흐름이 정상 동작하는지 검증하기 위해 작성했습니다.
	 * 1. 잔액이 정상적으로 충전된다.
	 * 2. 주문 및 결제한다.
	 * - 2-1. 주문을 한다.
	 * - 2-2. 재고를 차감한다.
	 * - 2-3. 잔액을 차감한다.
	 * - 2-4. 주문을 저장한다.
	 * - 2-5. 외부 전송을 위한 Message를 저장한다.
	 */
	@ScenarioIntegrationTest
	Stream<DynamicTest> 잔액을_충전하고_주문을_할_수_있다() {
		// given
		UserEntity user = userJpaRepository.save(UserEntity.builder()
			.email("user@gmail.com")
			.encryptedPassword("encrypted_password")
			.status(UserStatus.ACTIVE)
			.build());
		Long userId = user.getId();
		cashJpaRepository.save(CashEntity.builder()
			.balance(BigDecimal.ZERO)
			.userId(userId)
			.build());

		Product product = productJpaRepository.save(ProductEntity.builder()
			.name("오뚜기 진라면 매운맛 120g")
			.price(BigDecimal.valueOf(6_700))
			.stock(100)
			.build()).toDomain();

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

				List<CashHistoryEntity> cashHistories = cashHistoryRepository.findAllByUserId(userId);
				assertThat(cashHistories).hasSize(1);
				CashHistoryEntity cashHistory = cashHistories.get(0);
				assertThat(cashHistory.getUserId()).isEqualTo(userId);
				assertThat(cashHistory.getAction()).isEqualTo(CashHistoryAction.CHARGE);
				assertThat(cashHistory.getAmount().compareTo(BigDecimal.valueOf(10_000))).isZero().as("충전 금액");
				assertThat(cashHistory.getBalanceAfter().compareTo(BigDecimal.valueOf(10_000))).isZero().as("충전 이후 잔액");
			}),
			DynamicTest.dynamicTest("주문을 한다", () -> {
				// given
				final String idempotencyKey = "ORD_OAJOJNW_OJQOWJOA";
				final BigDecimal expectedPaymentAmount = BigDecimal.valueOf(6_700);
				final LocalDateTime now = LocalDateTime.now();
				// when
				Output output = transactionTemplate.execute(
					(status) -> {
						Command command = new Command(
							idempotencyKey,
							userId,
							null,
							expectedPaymentAmount,
							now,
							List.of(
								new OrderLineCommand(product.id(), 1)
							)
						);

						return orderPlaceProcessor.execute(command);
					});

				// then

				// (Order) 주문 저장 결과 확인
				Order order = output.order();
				assertThat(order.userId()).isEqualTo(userId);
				assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
				assertThat(order.amount().compareTo(BigDecimal.valueOf(6_700))).isZero();
				assertThat(order.orderLines()).hasSize(1);

				OrderLine orderLine = order.orderLines().get(0);
				assertThat(orderLine.productId()).isEqualTo(product.id());
				assertThat(orderLine.productName()).isEqualTo(product.name());
				assertThat(orderLine.orderQuantity()).isOne();
				assertThat(orderLine.totalAmount().compareTo(product.price())).isZero();

				// (Product) 상품 재고 차감 결과 확인
				assertThat(output.products().size()).isOne();
				Product productOfOutput = output.products().get(0);
				assertThat(productOfOutput.id()).isEqualTo(product.id());
				assertThat(productOfOutput.stock()).isEqualTo(99).as("100개 중 1개 주문하여 재고가 99개 남아야 합니다.");

				// (Cash) 잔액 차감 결과 확인
				CashEntity cash = output.cash();
				assertThat(cash.getUserId()).isEqualTo(userId);
				assertThat(cash.getBalance().compareTo(BigDecimal.valueOf(3_300))).isZero()
					.as("기존 잔액 10,000원에서 주문 가격 6,700원을 뺀 잔액입니다.");

				// (Message) 외부 전송을 위한 Message 저장 결과 확인
				List<MessageEntity> messages = messageJpaRepository.findAll();
				assertThat(messages.size()).isOne();

				MessageEntity message = messages.get(0);
				assertThat(message.getStatus()).isEqualTo(MessageStatus.PENDING);
				assertThat(message.getTargetId()).isEqualTo(order.id());
				assertThat(message.getTargetType()).isEqualTo(MessageTargetType.ORDER);
				assertThat(message.getType()).isEqualTo(MessageType.ORDER_CONFIRMED);

			})
		);
	}
}
