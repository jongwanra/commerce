package kr.hhplus.be.commerce.domain.coupon.event;

import java.time.LocalDateTime;

import kr.hhplus.be.commerce.domain.event.Event;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record CouponIssuedEvent(
	LocalDateTime occurredAt,
	String key,
	Long couponId,
	Long userId
) implements Event {
	public static Event of(Long couponId, Long userId, LocalDateTime occurredAt) {
		return CouponIssuedEvent.builder()
			.key("")
			.occurredAt(occurredAt)
			.couponId(couponId)
			.userId(userId)
			.build();
	}

}
