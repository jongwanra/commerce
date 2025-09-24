package kr.hhplus.be.commerce.presentation.api.coupon.controller;

import static kr.hhplus.be.commerce.application.coupon.UserCouponReadPageQueryManager.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.commerce.application.coupon.UserCouponReadPageQueryManager;
import kr.hhplus.be.commerce.presentation.api.coupon.controller.api.UserCouponReadPageApi;
import kr.hhplus.be.commerce.presentation.api.coupon.response.UserCouponSummaryResponse;
import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.presentation.global.request.CursorPaginationRequest;
import kr.hhplus.be.commerce.presentation.global.response.CursorPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserCouponReadPageController implements UserCouponReadPageApi {
	private final UserCouponReadPageQueryManager userCouponReadPageQueryManager;

	@Override
	@GetMapping("/api/v1/me/coupons")
	@ResponseStatus(HttpStatus.OK)
	public CursorPage<UserCouponSummaryResponse> readPage(
		@LoginUserId Long userId,
		@Valid CursorPaginationRequest cursorPaginationRequest
	) {

		log.info("cursorPaginationRequest = {}", cursorPaginationRequest);
		return userCouponReadPageQueryManager
			.read(new Query(
				userId,
				cursorPaginationRequest.lastId(),
				cursorPaginationRequest.size()
			));
	}

}
