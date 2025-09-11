package kr.hhplus.be.commerce.order.presentation.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.commerce.order.application.OrderPlaceProcessor;

public record OrderPlaceRequest(
	@NotNull(message = "orderLines is required")
	List<OrderLineRequest> orderLines
) {

	public record OrderLineRequest(
		@NotNull(message = "productId is required")
		@Schema(description = "주문할 상품 고유 식별자", example = "122")
		Long productId,

		@NotNull(message = "orderQuantity is required")
		@Schema(description = "주문 수량", example = "3")
		Integer orderQuantity
	) {

	}

	public OrderPlaceProcessor.Command toCommand(Long userId) {
		List<OrderPlaceProcessor.OrderLineCommand> orderLineCommands = orderLines.stream()
			.map(orderLine -> new OrderPlaceProcessor.OrderLineCommand(
				orderLine.productId(),
				orderLine.orderQuantity()
			))
			.toList();
		
		return new OrderPlaceProcessor.Command(userId, orderLineCommands);
	}
}
