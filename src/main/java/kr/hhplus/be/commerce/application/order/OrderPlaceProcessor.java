package kr.hhplus.be.commerce.application.order;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static kr.hhplus.be.commerce.presentation.global.utils.Validator.*;
import static kr.hhplus.be.commerce.presentation.global.utils.Validator.requireNonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

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
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashHistoryRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.CashRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashHistoryEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.UserCouponRepository;
import kr.hhplus.be.commerce.infrastructure.persistence.coupon.entity.UserCouponEntity;
import lombok.RequiredArgsConstructor;

/**
 * ref: https://discord.com/channels/1288769861589270590/1406891766153744485/1421467275982012577
 * 실무에서는 주문과 결제를 분리 하는 것이 상품의 재고를 미리 선점하고 결제를 이후에 진행할 수 있기 때문에 나은 방향이라고 생각합니다.
 * 하지만, 요구사항은  주문 + 결제 API를 통합 하는 방식으로 구현을 권장하고 있으며
 * 결제 시, 포인트 차감으로 외부 PG사에 의존하지 않기 때문에 주문과 결제를 통합하기로 결정했습니다.
 */

@RequiredArgsConstructor
public class OrderPlaceProcessor {
	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final ProductRepository productRepository;
	private final UserCouponRepository userCouponRepository;
	private final CashRepository cashRepository;
	private final CashHistoryRepository cashHistoryRepository;
	private final MessageRepository messageRepository;

	@Transactional
	public Output execute(Command command) {
		command.validate();

		/**
		 * TODO IdempotencyKey가 존재하지 않은 경우에는 Pessimistic Lock의 범위는 어떻게 잡힐까?
		 *
		 * 중복 호출인 경우 반환합니다.
		 */
		Optional<Order> alreadyPlacedOrderOpt = orderRepository.findByIdempotencyKeyWithLock(command.idempotencyKey);
		if (alreadyPlacedOrderOpt.isPresent()) {
			return Output.empty();
		}

		List<Long> productIds = command.toProductIds();
		List<Product> products = productRepository.findAllByIdInWithLock(productIds);

		if (products.size() != productIds.size()) {
			throw new CommerceException(CommerceCode.NOT_FOUND_PRODUCT);
		}

		Map<Long, Product> productIdToProductMap = products
			.stream()
			.collect(toMap(Product::id, product -> product));

		// 재고를 차감합니다.
		List<Product> deductedProducts = command.orderLineCommands()
			.stream()
			.map(orderLineCommand -> productIdToProductMap.get(orderLineCommand.productId())
				.deductStock(orderLineCommand.orderQuantity()))
			.toList();

		CashEntity cash = cashRepository.findByUserId(command.userId)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_CASH));

		Optional<UserCouponEntity> userCouponOpt =
			isNull(command.userCouponId) ? Optional.empty() :
				Optional.of(userCouponRepository.findById(command.userCouponId)
					.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_USER_COUPON)));

		// 주문 및 잔액을 차감합니다.
		// 주문 식별자를 미리 받기 위해서 save method를 호출합니다.
		Order order = orderRepository.save(
			Order.place(toOrderPlaceInput(command, deductedProducts, command.idempotencyKey)));

		messageRepository.save(
			Message.ofPending(
				order.id(),
				MessageTargetType.ORDER,
				OrderConfirmedMessagePayload.from(order)
			));

		return userCouponOpt.map(userCoupon -> executeWithCoupon(command, order, cash, userCoupon, deductedProducts))
			.orElseGet(() -> executeWithoutCoupon(command, order, cash, deductedProducts));
	}

	private Output executeWithoutCoupon(Command command, Order order, CashEntity cash, List<Product> products) {
		validatePaymentAmountIsMatched(command.paymentAmount, order);

		BigDecimal originalBalance = cash.getBalance();
		cash.use(command.paymentAmount);

		Payment payment = Payment.fromOrder(command.userId, order.id(), command.paymentAmount)
			.succeed(command.now);

		cashHistoryRepository.save(
			CashHistoryEntity.recordOfPurchase(command.userId, cash.getBalance(), originalBalance));

		return new Output(
			cashRepository.save(cash),
			null,
			productRepository.saveAll(products),
			paymentRepository.save(payment),
			order
		);
	}

	private Output executeWithCoupon(Command command, Order order, CashEntity cash,
		UserCouponEntity userCoupon, List<Product> products) {
		validatePaymentAmountIsMatched(command.paymentAmount, userCoupon, order);
		userCoupon.use(command.userId, command.now, order.id());

		BigDecimal originalBalance = cash.getBalance();
		cash.use(command.paymentAmount);

		Payment payment = Payment.fromOrder(command.userId, order.id(),
				command.paymentAmount)
			.succeed(command.now);

		cashHistoryRepository.save(
			CashHistoryEntity.recordOfPurchase(command.userId, cash.getBalance(), originalBalance));

		return new Output(
			cashRepository.save(cash),
			userCouponRepository.save(userCoupon),
			productRepository.saveAll(products),
			paymentRepository.save(payment),
			order
		);
	}

	private void validatePaymentAmountIsMatched(BigDecimal paymentAmount, UserCouponEntity userCoupon,
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

	public record Command(
		String idempotencyKey,
		Long userId,
		Long userCouponId,
		BigDecimal paymentAmount,
		LocalDateTime now,
		List<OrderLineCommand> orderLineCommands
	) {
		public void validate() {
			requireNonNull(List.of(Param.of(userId), Param.of(orderLineCommands)));
			if (orderLineCommands.isEmpty()) {
				throw new CommerceException(CommerceCode.ORDER_LINE_COMMANDS_IS_EMPTY);
			}
			orderLineCommands
				.forEach(it -> {
					if (isNull(it.orderQuantity()) || it.orderQuantity() <= 0) {
						throw new CommerceException(CommerceCode.ORDER_QUANTITY_MUST_BE_POSITIVE);
					}
				});
		}

		public List<Long> toProductIds() {
			return orderLineCommands.stream()
				.map(OrderLineCommand::productId)
				.toList();
		}
	}

	public record OrderLineCommand(
		Long productId,
		Integer orderQuantity
	) {
	}

	public record Output(
		CashEntity cash,
		UserCouponEntity userCoupon,
		List<Product> products,
		Payment payment,
		Order order
	) {
		public static Output empty() {
			return new Output(
				null,
				null,
				List.of(),
				null,
				null
			);
		}
	}
}
