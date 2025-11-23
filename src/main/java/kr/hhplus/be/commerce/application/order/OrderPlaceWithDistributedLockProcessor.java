package kr.hhplus.be.commerce.application.order;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import kr.hhplus.be.commerce.infrastructure.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ref: https://discord.com/channels/1288769861589270590/1406891766153744485/1421467275982012577
 * 실무에서는 주문과 결제를 분리 하는 것이 상품의 재고를 미리 선점하고 결제를 이후에 진행할 수 있기 때문에 나은 방향이라고 생각합니다.
 * 하지만, 요구사항은  주문 + 결제 API를 통합 하는 방식으로 구현을 권장하고 있으며
 * 결제 시, 포인트 차감으로 외부 PG사에 의존하지 않기 때문에 주문과 결제를 통합하기로 결정했습니다.
 *
 * @see kr.hhplus.be.commerce.application.message.publisher.OrderConfirmedMessagePublisher
 */

@Slf4j
@RequiredArgsConstructor
public class OrderPlaceWithDistributedLockProcessor implements OrderPlaceProcessor {
	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final ProductRepository productRepository;
	private final UserCouponRepository userCouponRepository;
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;
	private final MessageRepository messageRepository;
	private final UserRepository userRepository;

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
	@DistributedLock(
		key = "product",
		keyExpression = "#command.toProductIds()"
	)
	public Output execute(Command command) {
		command.validate();

		userRepository.findByIdForUpdate(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_USER));

		Optional<Order> alreadyPlacedOrderOpt = orderRepository.findByIdempotencyKey(command.idempotencyKey());
		if (alreadyPlacedOrderOpt.isPresent()) {
			return Output.empty();
		}

		// 상품의 비관적 잠금을 획득한 상태로 조회 및 재고를 감소시킵니다.
		List<Product> productsWithDecreasedStock = decreaseStock(command, fetchProductsForUpdate(command));

		Cash cash = cashRepository.findByUserId(command.userId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		// 주문 및 잔액을 차감합니다.
		// 주문 식별자를 미리 받기 위해서 save method를 호출합니다.
		Order order = orderRepository.save(Order.ofPending(command.userId()))
			.place(toOrderPlaceInput(command, productsWithDecreasedStock, command.idempotencyKey()));

		messageRepository.save(Message.ofPending(
			order.id(),
			MessageTargetType.ORDER,
			OrderConfirmedMessagePayload.from(order.id())
		));

		return isNull(command.userCouponId()) ?
			executeWithoutCoupon(command, order, cash, productsWithDecreasedStock) :
			executeWithCoupon(command, order, cash, productsWithDecreasedStock);
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

	private Output executeWithoutCoupon(Command command, Order order, Cash cash, List<Product> products) {
		validatePaymentAmountIsMatched(command.paymentAmount(), order);

		BigDecimal originalBalance = cash.balance();
		Cash usedCash = cash.use(command.paymentAmount());

		Payment payment = Payment.fromOrder(command.userId(), order.id(), command.paymentAmount())
			.succeed(command.now());

		cashHistoryRepository.save(
			CashHistory.recordOfPurchase(command.userId(), usedCash.balance(), originalBalance));

		return new Output(
			cashRepository.save(usedCash),
			null,
			productRepository.saveAll(products),
			paymentRepository.save(payment),
			orderRepository.save(order)
		);
	}

	private Output executeWithCoupon(Command command, Order order, Cash cash, List<Product> products) {
		UserCoupon userCoupon = userCouponRepository.findById(command.userCouponId())
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_USER_COUPON));

		validatePaymentAmountIsMatched(command.paymentAmount(), userCoupon, order);
		userCoupon.use(command.userId(), command.now(), order.id());

		BigDecimal originalBalance = cash.balance();
		Cash usedCash = cash.use(command.paymentAmount());

		Payment payment = Payment.fromOrder(command.userId(), order.id(),
				command.paymentAmount())
			.succeed(command.now());

		cashHistoryRepository.save(
			CashHistory.recordOfPurchase(command.userId(), usedCash.balance(), originalBalance));

		return new Output(
			cashRepository.save(cash),
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

	private OrderPlaceInput toOrderPlaceInput(Command command, List<Product> products, String idempotencyKey) {
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
