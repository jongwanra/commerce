package kr.hhplus.be.commerce.application.order;

import static kr.hhplus.be.commerce.application.order.OrderPlaceV1Processor.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.cash.repository.CashHistoryRepository;
import kr.hhplus.be.commerce.domain.cash.repository.CashRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.domain.user.repository.UserRepository;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.order.entity.OrderEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.product.entity.ProductEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;

class OrderPlaceProcessorConcurrencyIntegrationTest extends AbstractIntegrationTestSupport {
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

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		orderPlaceProcessor = new OrderPlaceV1Processor(orderRepository,
			paymentRepository,
			productRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository,
			messageRepository,
			userRepository
		);

	}

	/**
	 * [재고 감소 동시성 제어]
	 * 작성 이유: 100명의 사용자가 동시에 1개의 상품을 주문할 때, 재고 차감에 문제 발생 여부를 검증하고자 작성했습니다.
	 */
	@IntegrationTest
	void 사용자_100명이_동시에_10개의_재고를_가진_한_개의_상품을_주문할_때_10명의_사용자만_주문이_가능하다() throws InterruptedException {
		// given
		final int userCount = 100;
		BigDecimal balance = BigDecimal.valueOf(100_000);
		BigDecimal productPrice = BigDecimal.valueOf(10_000);
		final int remainingStock = 10;

		List<Long> userIds = new ArrayList<>(userCount);
		// 100명의 사용자가 10만원씩 잔액을 가지고 있다.
		for (int index = 0; index < userCount; index++) {
			UserEntity user = userJpaRepository.save(UserEntity.builder()
				.email("user" + index + "@gmail.com")
				.encryptedPassword("encrypted_password")
				.status(UserStatus.ACTIVE)
				.build());
			Long userId = user.getId();
			cashJpaRepository.save(
				CashEntity.fromDomain(Cash.restore(
					null,
					userId,
					balance,
					0L
				)));

			userIds.add(userId);
		}

		// 재고가 10개인 상품 한 개를 준비합니다.
		Product product = productJpaRepository.save(ProductEntity.builder()
			.name("오뚜기 진라면 매운맛 120g")
			.price(productPrice)
			.stock(remainingStock)
			.build()).toDomain();

		CountDownLatch countDownLatch = new CountDownLatch(userCount);
		ExecutorService executorService = Executors.newFixedThreadPool(userCount);

		// when
		IntStream.range(0, userCount)
			.forEach(index -> executorService.execute(() -> {
				try {
					transactionTemplate.executeWithoutResult(
						status -> orderPlaceProcessor.execute(generateCommand(index, userIds, product)));
				} catch (CommerceException e) {

				} finally {
					countDownLatch.countDown();
				}
			}));

		countDownLatch.await();

		// then
		ProductEntity afterProduct = productJpaRepository.findById(product.id())
			.orElseThrow();

		assertThat(afterProduct.getStock()).isZero();

		List<OrderEntity> orders = orderJpaRepository.findAll();
		assertThat(orders.size()).isEqualTo(10);

	}

	private Command generateCommand(int index, List<Long> userIds, Product product) {
		final String idempotencyKey = UUID.randomUUID().toString();
		final Long userId = userIds.get(index);
		final BigDecimal paymentAmount = BigDecimal.valueOf(10_000);
		final LocalDateTime now = LocalDateTime.now();
		List<OrderLineCommand> orderLineCommands = List.of(new OrderLineCommand(product.id(), 1));

		return new Command(idempotencyKey, userId, null, paymentAmount, now, orderLineCommands);
	}

}
