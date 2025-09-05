package kr.hhplus.be.commerce.coupon.presentation.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.coupon.application.CouponIssueProcessor;
import kr.hhplus.be.commerce.coupon.application.CouponIssueProcessor.Command;
import kr.hhplus.be.commerce.coupon.presentation.controller.api.CouponIssueApi;
import kr.hhplus.be.commerce.global.annotation.LoginUserId;
import kr.hhplus.be.commerce.global.response.EmptyResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CouponIssueController implements CouponIssueApi {
	private final CouponIssueProcessor couponIssueProcessor;

	@Override
	@PostMapping("/api/v1/me/coupons/{couponId}/issue")
	public EmptyResponse issue(@LoginUserId Long userId, @PathVariable("couponId") Long couponId) {
		LocalDateTime now = LocalDateTime.now();
		couponIssueProcessor.execute(new Command(
			userId,
			couponId,
			now
		));
		
		return EmptyResponse.INSTANCE;
	}
}
