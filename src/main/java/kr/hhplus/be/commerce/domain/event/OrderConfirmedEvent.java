package kr.hhplus.be.commerce.domain.event;

public record OrderConfirmedEvent(
	Long orderId
) implements Event {
	public static OrderConfirmedEvent withId(Long orderId) {
		return new OrderConfirmedEvent(orderId);
	}
}
