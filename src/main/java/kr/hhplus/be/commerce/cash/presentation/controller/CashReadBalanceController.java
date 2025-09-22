package kr.hhplus.be.commerce.cash.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.cash.application.CashReadBalanceQueryManager;
import kr.hhplus.be.commerce.cash.presentation.controller.api.CashReadMyBalanceApi;
import kr.hhplus.be.commerce.cash.presentation.response.CashDetailResponse;
import kr.hhplus.be.commerce.global.annotation.LoginUserId;
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
