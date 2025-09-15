package kr.hhplus.be.commerce.payment.presentation.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.commerce.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.global.response.EmptyResponse;
import kr.hhplus.be.commerce.payment.application.PaymentMakeProcessor;
import kr.hhplus.be.commerce.payment.presentation.controller.api.PaymentMakeApi;
import kr.hhplus.be.commerce.payment.presentation.request.PaymentMakeRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PaymentMakeController implements PaymentMakeApi {
	private final PaymentMakeProcessor paymentMakeProcessor;

	@Override
	@PostMapping("/api/v1/me/payments")
	@ResponseStatus(HttpStatus.CREATED)
	public EmptyResponse makePayment(@LoginUserId Long userId, @Valid @RequestBody PaymentMakeRequest request) {
		paymentMakeProcessor.execute(request.toCommand(userId, LocalDateTime.now()));
		return EmptyResponse.INSTANCE;
	}
}
