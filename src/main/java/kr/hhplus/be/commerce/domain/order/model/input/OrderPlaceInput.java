package kr.hhplus.be.commerce.domain.order.model.input;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;

@Builder
public record OrderPlaceInput(
	Long userId,
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
