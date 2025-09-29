package kr.hhplus.be.commerce.application.payment;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import net.bytebuddy.utility.RandomString;

import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.outbox_event.recorder.EventRecorder;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.product.entity.ProductEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.UserJpaRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;

class PaymentMakeProcessorIntegrationTest extends AbstractIntegrationTestSupport {
	private OrderPlaceProcessor orderPlaceProcessor;
	private PaymentMakeProcessor paymentMakeProcessor;

	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private UserCouponRepository userCouponRepository;
	@Autowired
	private CashRepository cashRepository;
	@Autowired
	private CashHistoryRepository cashHistoryRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private EventRecorder eventRecorder;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		orderPlaceProcessor = new OrderPlaceProcessor(
			orderRepository,
			productRepository
		);

		paymentMakeProcessor = new PaymentMakeProcessor(
			paymentRepository,
			orderRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository,
			eventRecorder
		);
	}

	@IntegrationTest
	void 중복_결제를_시도할_경우_동일한_결과값을_제공한다() {
		// given
		final LocalDateTime now = LocalDateTime.now();
		UserEntity user = userJpaRepository.save(UserEntity
			.builder()
			.email("user.a@gmail.com")
			.encryptedPassword(RandomString.make(20))
			.status(UserStatus.ACTIVE)
			.build());

		cashJpaRepository.save(
			CashEntity.builder()
				.balance(BigDecimal.valueOf(10_000))
				.userId(user.getId())
				.build()
		);

		Product product = productJpaRepository.save(ProductEntity.builder()
				.name("오뚜기 진라면 매운맛 120g")
				.price(BigDecimal.valueOf(6_700))
				.stock(100)
				.build())
			.toDomain();

		// 상품을 주문합니다.
		OrderPlaceProcessor.Output outputOfOrder = transactionTemplate.execute(
			(status -> orderPlaceProcessor.execute(new OrderPlaceProcessor.Command(
				user.getId(),
				List.of(
					new OrderPlaceProcessor.OrderLineCommand(product.id(), 1)
				)
			))));

		final String idempotencyKey = UUID.randomUUID().toString()
			.replace("-", "")
			.substring(0, 14);

		// 주문건에 대한 결제를 합니다.
		PaymentMakeProcessor.Command command = new PaymentMakeProcessor.Command(
			idempotencyKey,
			user.getId(),
			outputOfOrder.order().id(),
			null,
			outputOfOrder.order().amount(),
			now
		);

		PaymentMakeProcessor.Output outputOfFirstPayment = transactionTemplate.execute(
			(status -> paymentMakeProcessor.execute(command)));

		// when
		// 중복 결제를 시도합니다. (동일한 멱등키를 이용합니다.)
		PaymentMakeProcessor.Output outputOfDuplicatedPayment = transactionTemplate.execute(
			(status -> paymentMakeProcessor.execute(command)));

		// then
		assertThat(outputOfFirstPayment.cash().getBalance().compareTo(outputOfDuplicatedPayment.cash().getBalance()))
			.isZero().as("중복 결제 이후에도 남은 사용자의 잔액은 동일합니다.");
		assertThat(outputOfFirstPayment.userCoupon()).isEqualTo(outputOfDuplicatedPayment.userCoupon());

		assertThat(outputOfFirstPayment.order().orderLines().size()).isEqualTo(
			outputOfDuplicatedPayment.order().orderLines().size());

		assertThat(
			outputOfFirstPayment.order().amount().compareTo(outputOfDuplicatedPayment.order().amount())).isZero();
		assertThat(
			outputOfFirstPayment.payment().amount().compareTo(outputOfDuplicatedPayment.payment().amount())).isZero();
	}

}
