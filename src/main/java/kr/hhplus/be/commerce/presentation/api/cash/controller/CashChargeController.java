package kr.hhplus.be.commerce.presentation.api.cash.controller;

import static kr.hhplus.be.commerce.application.cash.CashChargeProcessor.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.commerce.application.cash.CashChargeProcessor;
import kr.hhplus.be.commerce.presentation.api.cash.controller.api.CashChargeApi;
import kr.hhplus.be.commerce.presentation.api.cash.response.CashChargeRequest;
import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.presentation.global.response.EmptyResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CashChargeController implements CashChargeApi {
	private final CashChargeProcessor cashChargeProcessor;

	@Override
	@PatchMapping("/api/v1/me/cash/charge")
	@ResponseStatus(HttpStatus.OK)
	public EmptyResponse charge(
		@LoginUserId Long userId,
		@Valid @RequestBody CashChargeRequest request
	) {
		Command command = new Command(userId, request.amount());
		cashChargeProcessor.execute(command);

		return EmptyResponse.INSTANCE;
	}
}
