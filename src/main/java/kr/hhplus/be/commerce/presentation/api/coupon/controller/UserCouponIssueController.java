package kr.hhplus.be.commerce.presentation.api.coupon.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.application.coupon.UserCouponIssueProcessor;
import kr.hhplus.be.commerce.application.coupon.UserCouponIssueProcessor.Command;
import kr.hhplus.be.commerce.application.coupon.UserCouponIssueWithEventProcessor;
import kr.hhplus.be.commerce.presentation.api.coupon.controller.api.UserCouponIssueApi;
import kr.hhplus.be.commerce.presentation.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.presentation.global.response.EmptyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserCouponIssueController implements UserCouponIssueApi {
	private final UserCouponIssueProcessor userCouponIssueProcessor;
	private final UserCouponIssueWithEventProcessor userCouponIssueWithEventProcessor;

	@Override
	@PostMapping("/api/v1/me/coupons/{couponId}/issue")
	@ResponseStatus(HttpStatus.CREATED)
	public EmptyResponse issue(@LoginUserId Long userId, @PathVariable("couponId") Long couponId) {
		LocalDateTime now = LocalDateTime.now();
		userCouponIssueProcessor.execute(new Command(
			userId,
			couponId,
			now
		));

		return EmptyResponse.INSTANCE;
	}

	@PostMapping("/api/v2/me/coupons/{couponId}/issue")
	@ResponseStatus(HttpStatus.CREATED)
	public EmptyResponse issueV2(@LoginUserId Long userId, @PathVariable("couponId") Long couponId) {
		LocalDateTime now = LocalDateTime.now();
		userCouponIssueWithEventProcessor.execute(new UserCouponIssueWithEventProcessor.Command(
			userId,
			couponId,
			now
		));

		return EmptyResponse.INSTANCE;
	}
}
