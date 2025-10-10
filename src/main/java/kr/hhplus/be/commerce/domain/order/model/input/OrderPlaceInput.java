package kr.hhplus.be.commerce.domain.order.model.input;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import kr.hhplus.be.commerce.domain.order.policy.DiscountAmountCalculable;
import lombok.Builder;

@Builder
public record OrderPlaceInput(
	Long userId,
	String idempotencyKey,
	LocalDateTime now,
	DiscountAmountCalculable discountAmountCalculable,
	List<OrderLineInput> orderLineInputs
) {

	@Builder
	public record OrderLineInput(
		Long productId,
		String productName,
		BigDecimal productPrice,
		Integer orderQuantity
	) {

	}
}
