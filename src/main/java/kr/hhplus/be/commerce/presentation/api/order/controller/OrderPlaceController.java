package kr.hhplus.be.commerce.presentation.api.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.commerce.application.order.OrderPlaceProcessor;
import kr.hhplus.be.commerce.presentation.api.order.controller.api.OrderPlaceApi;
import kr.hhplus.be.commerce.presentation.api.order.request.OrderPlaceRequest;
import kr.hhplus.be.commerce.presentation.global.annotation.IdempotencyKey;
import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.presentation.global.response.EmptyResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderPlaceController implements OrderPlaceApi {
	private final OrderPlaceProcessor orderPlaceProcessor;

	@Override
	@PostMapping("/api/v1/me/orders")
	@ResponseStatus(HttpStatus.CREATED)
	public EmptyResponse placeOrder(
		@LoginUserId Long userId,
		@IdempotencyKey String idempotencyKey,
		@Valid @RequestBody OrderPlaceRequest request) {
		orderPlaceProcessor.execute(request.toCommand(userId, idempotencyKey));
		return EmptyResponse.INSTANCE;
	}
}
