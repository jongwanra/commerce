package kr.hhplus.be.commerce.application.order;

import static kr.hhplus.be.commerce.application.order.OrderPlaceWithDatabaseLockProcessor.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.bytebuddy.utility.RandomString;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.cash.repository.CashHistoryRepository;
import kr.hhplus.be.commerce.domain.cash.repository.CashRepository;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.domain.user.model.User;
import kr.hhplus.be.commerce.domain.user.repository.UserRepository;
import kr.hhplus.be.commerce.global.AbstractUnitTestSupport;
import kr.hhplus.be.commerce.global.time.FixedTimeProvider;
import kr.hhplus.be.commerce.global.time.TimeProvider;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;

@ExtendWith(MockitoExtension.class)
class OrderPlaceProcessorUnitTest extends AbstractUnitTestSupport {
	private OrderPlaceProcessor orderPlaceProcessor;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private UserCouponRepository userCouponRepository;
	@Mock
	private CashRepository cashRepository;
	@Mock
	private CashHistoryRepository cashHistoryRepository;
	@Mock
	private MessageRepository messageRepository;

	@Mock
	private UserRepository userRepository;

	private TimeProvider timeProvider;

	@BeforeEach
	void setUp() {
		timeProvider = FixedTimeProvider.of(LocalDateTime.of(2025, 12, 1, 0, 0, 0));
		orderPlaceProcessor = new OrderPlaceWithDatabaseLockProcessor(
			orderRepository,
			paymentRepository,
			productRepository,
			userCouponRepository,
			cashRepository,
			cashHistoryRepository,
			messageRepository,
			userRepository,
			timeProvider
		);
	}

	// 작성 이유: 주문하고자 하는 상품이 존재하지 않는 상품인 경우 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 주문할_상품이_존재하지_않는_경우_예외를_발생_시킨다() {
		// given
		Long notExistProductId = 999L;
		final String idempotencyKey = "ORD_250930_AOMEWD";
		Command command = new Command(idempotencyKey, 1L, 100L, BigDecimal.valueOf(3_000),
			List.of(new OrderLineCommand(notExistProductId, 1)));

		// mock
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.of(User.restore(1L, UserStatus.ACTIVE, "jongwan.ra@gmail.com", RandomString.make())));
		given(productRepository.findAllByIdInForUpdate(List.of(notExistProductId)))
			.willReturn(List.of());

		// when & then
		assertThatThrownBy(() -> orderPlaceProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("존재하지 않는 상품입니다.");
	}

	// 작성 이유: 주문할 상품의 재고가 부족한 경우 예외를 발생시키는지 검증하기 위해 작성했습니다.
	// [경계값: 재고: 1, 주문 수량:2]
	@Test
	void 주문할_상품의_재고가_주문_수량보다_부족한_경우_예외를_발생_시킨다() {
		// given
		Long productId = 999L;
		Long userId = 1L;
		final int remainingStock = 1;
		final int orderQuantity = 2; // 재고보다 많은 수량 주문

		final String idempotencyKey = "ORD_250930_AOMEWD";

		Product product = Product.restore(productId, "product name", remainingStock,
			BigDecimal.valueOf(10_000),
			LocalDateTime.now());

		BigDecimal paymentAmount = BigDecimal.valueOf(20_000);

		Command command = new Command(idempotencyKey, userId, null, paymentAmount,
			List.of(new OrderLineCommand(productId, orderQuantity)));

		// mock
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.of(User.restore(1L, UserStatus.ACTIVE, "jongwan.ra@gmail.com", RandomString.make())));
		given(productRepository.findAllByIdInForUpdate(List.of(productId)))
			.willReturn(List.of(product));

		// when & then
		assertThatThrownBy(() -> orderPlaceProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("상품의 재고가 부족합니다.");
	}

	// 작성 이유: 주문할 상품의 재고와 주문 수량이 같은 경우 주문이 가능 한지 확인하기 위해 작성했습니다.
	// [경계값: 재고: 1, 주문 수량:1]
	@Test
	void 주문할_상품의_재고가_주문_수량과_일치한_경우_주문할_수_있다() {
		// given
		Long productId = 999L;
		Long userId = 1L;
		final int stock = 1;
		final int orderQuantity = 1; // 재고와 동일한 주문 수량
		Long orderId = 1L;
		Long orderLineId = 1L;
		LocalDateTime createdAt = LocalDateTime.now();
		Product product = Product.restore(productId, "product name", stock, BigDecimal.valueOf(10_000),
			createdAt);

		Long cashId = 233L;
		Cash cash = Cash.restore(
			cashId, userId, BigDecimal.valueOf(200_000), 0L
		);

		final String idempotencyKey = "ORD_250930_AOMEWD";
		LocalDateTime now = LocalDateTime.now();
		BigDecimal paymentAmount = BigDecimal.valueOf(10_000);

		Command command = new Command(idempotencyKey, userId, null, paymentAmount,
			List.of(new OrderLineCommand(productId, orderQuantity)));
		// mock
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.of(User.restore(1L, UserStatus.ACTIVE, "jongwan.ra@gmail.com", RandomString.make())));
		
		given(productRepository.findAllByIdInForUpdate(List.of(productId)))
			.willReturn(List.of(product));

		Product savedProduct = Product.restore(productId, "product name", stock - orderQuantity,
			BigDecimal.valueOf(10_000),
			createdAt);

		given(productRepository.saveAll(any()))
			.willReturn(List.of(savedProduct));

		given(cashRepository.findByUserId(anyLong()))
			.willReturn(Optional.of(cash));

		OrderLine savedOrderLine = OrderLine.restore(
			orderLineId,
			orderId,
			productId,
			"product name",
			BigDecimal.valueOf(10_000),
			orderQuantity
		);

		Order savedOrder = Order.restore(
			orderId,
			userId,
			OrderStatus.PENDING,
			BigDecimal.valueOf(10_000),
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			List.of(savedOrderLine),
			null,
			idempotencyKey
		);

		given(orderRepository.save(any()))
			.willReturn(savedOrder);
		// when
		Output output = orderPlaceProcessor.execute(command);

		// then
		List<Product> products = output.products();
		assertThat(products.size()).isOne();
		assertThat(products.get(0).stock()).isZero();
		assertThat(products.get(0).id()).isEqualTo(productId);
		assertThat(products.get(0).name()).isEqualTo("product name");
		assertThat(products.get(0).price()).isEqualByComparingTo(BigDecimal.valueOf(10_000));

		Order order = output.order();
		assertThat(order.userId()).isEqualTo(userId);
		assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
		assertThat(order.amount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
		assertThat(order.id()).isEqualTo(1L);
		assertThat(order.orderLines().size()).isOne();

		OrderLine orderLine = order.orderLines().get(0);
		assertThat(orderLine.id()).isEqualTo(1L);
		assertThat(orderLine.productId()).isEqualTo(productId);
		assertThat(orderLine.productName()).isEqualTo("product name");
		assertThat(orderLine.productAmount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
		assertThat(orderLine.orderQuantity()).isEqualTo(1);

	}

	// 작성 이유: 주문할 상품의 수량이 양수가 아닌 경우 예외를 발생시키는지 검증 [경계값: 0]
	@Test
	void 주문할_상품의_수량이_양수가_아닌_경우_예외를_발생_시킨다() {
		// given
		final int zeroOrderQuantity = 0;
		final String idempotencyKey = "ORD_250930_AOMEWD";
		LocalDateTime now = LocalDateTime.now();
		BigDecimal paymentAmount = BigDecimal.valueOf(20_000);

		List<OrderLineCommand> orderLineCommands = List.of(new OrderLineCommand(1L, zeroOrderQuantity));
		Command command = new Command(idempotencyKey, 1L, null, paymentAmount, orderLineCommands);

		// when & then
		assertThatThrownBy(() -> {
			orderPlaceProcessor.execute(command);
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("주문 수량은 1개 이상이어야 합니다.");
	}

	// 작성 이유: 결제 금액과 주문 금액이 일치하는지 검증하기 위해서 작성했습니다. [쿠폰 사용 X]
	@Test
	void 결제_금액과_주문_금액이_다를_경우_예외를_발생시킨다() {
	}

	// 작성 이유: 존재하지 않는 쿠폰을 사용하려고 할 때 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 존재하지_않는_사용자_쿠폰을_사용할_경우_예외를_발생시킨다() {

	}

	// 작성 이유: 다른 사용자의 쿠폰을 사용하려고할 때 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 다른_사용자의_쿠폰을_사용할_경우에_예외를_발생시킨다() {
	}

	// 작성 이유: 사용자의 잔액이 부족할 때 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 결제할_잔액이_부족한_경우_예외를_발생시킨다() {

	}
}
