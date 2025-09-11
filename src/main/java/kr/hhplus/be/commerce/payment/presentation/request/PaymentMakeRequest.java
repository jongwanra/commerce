package kr.hhplus.be.commerce.payment.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PaymentMakeRequest(
	@Schema(description = "결제할 주문 고유 식별자", example = "456")
	@NotNull(message = "orderId is required")
	Long orderId,
	@Schema(description = "쿠폰 고유 식별자", nullable = true, example = "789")
	Long couponId,

	@Schema(description = "예상 결제 금액 클라이언트/서버 간 금액 일치 여부 확인", example = "15000")
	@NotNull(message = "expectedAmount is required")
	Long expectedAmount
) {
}
