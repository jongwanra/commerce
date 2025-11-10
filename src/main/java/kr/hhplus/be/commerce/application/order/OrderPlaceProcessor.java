package kr.hhplus.be.commerce.application.order;

import static java.util.Objects.*;
import static kr.hhplus.be.commerce.presentation.global.utils.Validator.*;
import static kr.hhplus.be.commerce.presentation.global.utils.Validator.requireNonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import kr.hhplus.be.commerce.domain.cash.model.Cash;
import kr.hhplus.be.commerce.domain.coupon.model.UserCoupon;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.payment.model.Payment;
import kr.hhplus.be.commerce.domain.product.model.Product;

/**
 * ref: https://discord.com/channels/1288769861589270590/1406891766153744485/1421467275982012577
 * 실무에서는 주문과 결제를 분리 하는 것이 상품의 재고를 미리 선점하고 결제를 이후에 진행할 수 있기 때문에 나은 방향이라고 생각합니다.
 * 하지만, 요구사항은  주문 + 결제 API를 통합 하는 방식으로 구현을 권장하고 있으며
 * 결제 시, 포인트 차감으로 외부 PG사에 의존하지 않기 때문에 주문과 결제를 통합하기로 결정했습니다.
 */

public interface OrderPlaceProcessor {
	Output execute(Command command);

	record Command(
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

	record OrderLineCommand(
		Long productId,
		Integer orderQuantity
	) {
	}

	record Output(
		Cash cash,
		UserCoupon userCoupon,
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
