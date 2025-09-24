package kr.hhplus.be.commerce.presentation.api.payment.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.commerce.application.payment.PaymentMakeProcessor;

public record PaymentMakeRequest(
	@Schema(description = "결제할 주문 고유 식별자", example = "456")
	@NotNull(message = "orderId is required")
	Long orderId,
	@Schema(description = "사용자 쿠폰 고유 식별자", nullable = true, example = "789")
	Long userCouponId,

	@Schema(description = "예상 결제 금액 클라이언트/서버 간 금액 일치 여부 확인", example = "15000")
	@NotNull(message = "expectedPaymentAmount is required")
	BigDecimal expectedPaymentAmount
) {
	public PaymentMakeProcessor.Command toCommand(Long userId, LocalDateTime now) {
		return new PaymentMakeProcessor.Command(
			userId,
			orderId,
			userCouponId,
			expectedPaymentAmount,
			now
		);
	}
}
