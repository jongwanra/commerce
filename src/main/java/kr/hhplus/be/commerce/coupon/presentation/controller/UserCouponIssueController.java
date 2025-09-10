package kr.hhplus.be.commerce.coupon.presentation.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.coupon.application.UserCouponIssueProcessor;
import kr.hhplus.be.commerce.coupon.application.UserCouponIssueProcessor.Command;
import kr.hhplus.be.commerce.coupon.presentation.controller.api.UserCouponIssueApi;
import kr.hhplus.be.commerce.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.global.response.EmptyResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserCouponIssueController implements UserCouponIssueApi {
	private final UserCouponIssueProcessor userCouponIssueProcessor;

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
}
