package kr.hhplus.be.commerce.presentation.api.cash.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.application.cash.CashReadBalanceQueryManager;
import kr.hhplus.be.commerce.presentation.api.cash.controller.api.CashReadMyBalanceApi;
import kr.hhplus.be.commerce.presentation.api.cash.response.CashDetailResponse;
import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CashReadBalanceController implements CashReadMyBalanceApi {
	private final CashReadBalanceQueryManager cashReadBalanceQueryManager;

	@Override
	@GetMapping("/api/v1/me/cash")
	@ResponseStatus(HttpStatus.OK)
	public CashDetailResponse readBalance(@LoginUserId Long userId) {
		return cashReadBalanceQueryManager.read(userId);
	}
}
