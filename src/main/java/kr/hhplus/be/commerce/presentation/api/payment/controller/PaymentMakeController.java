package kr.hhplus.be.commerce.presentation.api.payment.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.commerce.application.payment.PaymentMakeProcessor;
import kr.hhplus.be.commerce.presentation.api.payment.controller.api.PaymentMakeApi;
import kr.hhplus.be.commerce.presentation.api.payment.request.PaymentMakeRequest;
import kr.hhplus.be.commerce.presentation.global.annotation.IdempotencyKey;
import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.presentation.global.response.EmptyResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PaymentMakeController implements PaymentMakeApi {
	private final PaymentMakeProcessor paymentMakeProcessor;

	@Override
	@PostMapping("/api/v1/me/payments")
	@ResponseStatus(HttpStatus.CREATED)
	public EmptyResponse makePayment(@LoginUserId Long userId, @IdempotencyKey String idempotencyKey,
		@Valid @RequestBody PaymentMakeRequest request) {
		paymentMakeProcessor.execute(request.toCommand(idempotencyKey, userId, LocalDateTime.now()));
		return EmptyResponse.INSTANCE;
	}
}
