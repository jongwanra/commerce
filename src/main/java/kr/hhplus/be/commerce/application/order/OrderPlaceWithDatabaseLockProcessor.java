package kr.hhplus.be.commerce.application.order;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.cash.model.CashHistory;
import kr.hhplus.be.commerce.domain.cash.repository.CashHistoryRepository;
import kr.hhplus.be.commerce.domain.cash.repository.CashRepository;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.message.enums.MessageTargetType;
import kr.hhplus.be.commerce.domain.message.model.Message;
import kr.hhplus.be.commerce.domain.message.model.message_payload.OrderConfirmedMessagePayload;
import kr.hhplus.be.commerce.domain.message.repository.MessageRepository;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.input.OrderPlaceInput;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.payment.model.Payment;
import kr.hhplus.be.commerce.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import kr.hhplus.be.commerce.domain.user.repository.UserRepository;
import kr.hhplus.be.commerce.global.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 1. 멱등 조회(findByIdempotencyKey) 먼저, 있으면 즉시 반환.
 * 2. user는 단순 조회.
 * 3. 실제 공유 자원인 product 재고(비관적), cash/user_coupon은 낙관적 버전+재시도 또는 비관적 락으로 보호.
 * 4. orderRepository.save 시 유니크 제약 위반을 잡아 멱등 처리로 변환.
 */

@Slf4j
@RequiredArgsConstructor
public class OrderPlaceWithDatabaseLockProcessor implements OrderPlaceProcessor {
	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final ProductRepository productRepository;
	private final UserCouponRepository userCouponRepository;
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;
	private final MessageRepository messageRepository;
	private final UserRepository userRepository;
	private final TimeProvider timeProvider;

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
		command.validate();
		LocalDateTime now = timeProvider.now();
		LocalDate today = timeProvider.today();

		// 멱등키 조회를 통해 중복 결제인 경우 즉시 반환합니다.
		if (isAlreadyPlacedOrder(command.idempotencyKey())) {
			return Output.empty();
		}

		userRepository.findById(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_USER));

		// 상품의 비관적 잠금을 획득한 상태로 조회 및 재고를 감소시킵니다.
		List<Product> productsWithDecreasedStock = decreaseStock(command, fetchProductsForUpdate(command));

		Cash cash = cashRepository.findByUserId(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		// 주문 및 잔액을 차감합니다.
		// 주문 식별자를 미리 받기 위해서 save method를 호출합니다.
		Order order;
		try {
			order = orderRepository.save(Order.ofPending(command.userId()));
		} catch (DataIntegrityViolationException e) {
			log.warn("중복 결제 건이 있어서 반환합니다. idempotencyKey={}", command.idempotencyKey());
			return Output.empty();
		}
		Order placedOrder = order.place(
			buildOrderPlaceInput(command, productsWithDecreasedStock, command.idempotencyKey())).order();

		log.debug("[+{}] 주문을 처리했습니다.", Thread.currentThread().getName());

		messageRepository.save(Message.ofPending(
			order.id(),
			MessageTargetType.ORDER,
			OrderConfirmedMessagePayload.from(order.id(), today, now)
		));
		log.debug("[+{}] 메세지를 저장했습니다.", Thread.currentThread().getName());

		return processPayment(command, placedOrder, cash, productsWithDecreasedStock, now);

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

	private Output processPayment(Command command, Order placedOrder, Cash cash,
		List<Product> productsWithDecreasedStock, LocalDateTime now) {
		return isNull(command.userCouponId()) ?
			processPaymentWithoutCoupon(command, placedOrder, cash, productsWithDecreasedStock, now) :
			processPaymentWithCoupon(command, placedOrder, cash, productsWithDecreasedStock, now);
	}

	private boolean isAlreadyPlacedOrder(String idempotencyKey) {
		return orderRepository.findByIdempotencyKey(idempotencyKey).isPresent();
	}

	private List<Product> fetchProductsForUpdate(Command command) {
		List<Long> productIds = command.toProductIds();
		List<Product> products = productRepository.findAllByIdInForUpdate(productIds);
		if (products.size() != productIds.size()) {
			throw new CommerceException(CommerceCode.NOT_FOUND_PRODUCT);
		}
		return products;
	}

	private List<Product> decreaseStock(Command command, List<Product> products) {
		Map<Long, Product> productIdToProductMap = products
			.stream()
			.collect(toMap(Product::id, product -> product));

		// 재고를 차감합니다.
		return command.orderLineCommands()
			.stream()
			.map(orderLineCommand -> {
				Product product = productIdToProductMap.get(orderLineCommand.productId());
				return product.decreaseStock(orderLineCommand.orderQuantity());
			})
			.toList();
	}

	private Output processPaymentWithoutCoupon(Command command, Order order, Cash cash, List<Product> products,
		LocalDateTime now) {
		validatePaymentAmountIsMatched(command.paymentAmount(), order);

		BigDecimal originalBalance = cash.balance();
		Cash usedCash = cash.use(command.paymentAmount());

		Payment payment = Payment.fromOrder(command.userId(), order.id(), command.paymentAmount())
			.succeed(now);

		return new Output(
			saveCashWithHistory(command, usedCash, originalBalance),
			null,
			productRepository.saveAll(products),
			paymentRepository.save(payment),
			orderRepository.save(order)
		);
	}

	private Output processPaymentWithCoupon(Command command, Order order, Cash cash, List<Product> products,
		LocalDateTime now) {
		UserCoupon userCoupon = userCouponRepository.findById(command.userCouponId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_USER_COUPON));

		validatePaymentAmountIsMatched(command.paymentAmount(), userCoupon, order);
		userCoupon.use(command.userId(), now, order.id());

		BigDecimal originalBalance = cash.balance();
		Cash usedCash = cash.use(command.paymentAmount());

		Payment payment = Payment.fromOrder(command.userId(), order.id(),
				command.paymentAmount())
			.succeed(now);
		
		return new Output(
			saveCashWithHistory(command, usedCash, originalBalance),
			userCouponRepository.save(userCoupon),
			productRepository.saveAll(products),
			paymentRepository.save(payment),
			orderRepository.save(order)
		);
	}

	private void validatePaymentAmountIsMatched(BigDecimal paymentAmount, UserCoupon userCoupon,
		Order order) {
		BigDecimal actualPaymentAmount = userCoupon.calculateFinalAmount(order.amount());
		if (paymentAmount.compareTo(actualPaymentAmount) != 0) {
			throw new CommerceException(CommerceCode.MISMATCHED_EXPECTED_AMOUNT);
		}
	}

	private void validatePaymentAmountIsMatched(BigDecimal paymentAmount, Order order) {
		BigDecimal actualPaymentAmount = order.amount();
		if (paymentAmount.compareTo(actualPaymentAmount) != 0) {
			throw new CommerceException(CommerceCode.MISMATCHED_EXPECTED_AMOUNT);
		}
	}

	private Cash saveCashWithHistory(Command command, Cash usedCash, BigDecimal originalBalance) {
		cashHistoryRepository.save(
			CashHistory.recordOfPurchase(command.userId(), usedCash.balance(), originalBalance));

		return cashRepository.save(usedCash);
	}

	private OrderPlaceInput buildOrderPlaceInput(Command command, List<Product> products, String idempotencyKey) {
		Map<Long, Product> productIdToProductMap = products
			.stream()
			.collect(toMap(Product::id, product -> product));

		return OrderPlaceInput.builder()
			.idempotencyKey(idempotencyKey)
			.userId(command.userId())
			.orderLineInputs(command.orderLineCommands()
				.stream()
				.map(orderLineCommand -> {
					Product product = productIdToProductMap.get(orderLineCommand.productId());
					return OrderPlaceInput.OrderLineInput
						.builder()
						.productId(product.id())
						.productName(product.name())
						.productPrice(product.price())
						.orderQuantity(orderLineCommand.orderQuantity())
						.build();
				})
				.toList())
			.build();
	}

}
