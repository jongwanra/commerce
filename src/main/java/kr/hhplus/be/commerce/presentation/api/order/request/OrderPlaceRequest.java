package kr.hhplus.be.commerce.presentation.api.order.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;

public record OrderPlaceRequest(
	@Schema(description = "사용자 쿠폰 고유 식별자", nullable = true, example = "789")
	Long userCouponId,

	@Schema(description = "예상 결제 금액 클라이언트/서버 간 금액 일치 여부 확인", example = "15000")
	@NotNull(message = "expectedPaymentAmount is required")
	BigDecimal expectedPaymentAmount,

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

	public OrderPlaceProcessor.Command toCommand(Long userId, String idempotencyKey) {
		List<OrderPlaceProcessor.OrderLineCommand> orderLineCommands = orderLines.stream()
			.map(orderLine -> new OrderPlaceProcessor.OrderLineCommand(
				orderLine.productId(),
				orderLine.orderQuantity()
			))
			.toList();

		return new OrderPlaceProcessor.Command(idempotencyKey, userId, userCouponId, expectedPaymentAmount,
			LocalDateTime.now(),
			orderLineCommands);
	}
}
