package kr.hhplus.be.commerce.order.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import kr.hhplus.be.commerce.coupon.persistence.UserCouponEntity;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import kr.hhplus.be.commerce.order.domain.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Order {
	private Long id;
	private Long userId;
	private OrderStatus status;
	// 주문가, 주문 라인의 상품 가격 * 주문 수량을 전부 더한 가격
	private BigDecimal amount;
	// 할인 가격(쿠폰 등)
	private BigDecimal discountAmount;
	// 최종 결제 가격(= amount - discountAmount)
	private BigDecimal finalAmount;
	private List<OrderLine> orderLines = new ArrayList<>();
	private LocalDateTime confirmedAt;

	protected Order() {
	}

	@Builder
	private Order(Long id, Long userId, OrderStatus status, BigDecimal amount, BigDecimal discountAmount,
		BigDecimal finalAmount, List<OrderLine> orderLines, LocalDateTime confirmedAt) {
		this.id = id;
		this.userId = userId;
		this.status = status;
		this.amount = amount;
		this.discountAmount = discountAmount;
		this.finalAmount = finalAmount;
		this.orderLines = orderLines;
		this.confirmedAt = confirmedAt;
	}

	public static Order place(Long userId, List<OrderLine> orderLines) {
		Order order = new Order();
		order.userId = userId;
		order.status = OrderStatus.PENDING;
		order.orderLines = orderLines;
		order.amount = order.calculateTotalAmount();
		order.discountAmount = BigDecimal.ZERO;
		order.finalAmount = BigDecimal.ZERO;
		return order;
	}

	private BigDecimal calculateTotalAmount() {
		return this.orderLines.stream()
			.map(OrderLine::getTotalAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void authorize(Long userId) {
		if (this.userId.equals(userId)) {
			return;
		}
		throw new CommerceException(CommerceCode.UNAUTHORIZED_USER);
	}

	public void confirm(LocalDateTime now, UserCouponEntity userCoupon) {
		if (status.isConfirmed()) {
			throw new CommerceException(CommerceCode.ALREADY_CONFIRMED_ORDER);
		}
		this.status = OrderStatus.CONFIRMED;
		this.discountAmount = userCoupon.calculateDiscountAmount(this.amount);
		this.finalAmount = this.amount.subtract(this.discountAmount);
		this.confirmedAt = now;
	}

	public void confirm(LocalDateTime now) {
		if (status.isConfirmed()) {
			throw new CommerceException(CommerceCode.ALREADY_CONFIRMED_ORDER);
		}
		this.status = OrderStatus.CONFIRMED;
		this.discountAmount = BigDecimal.ZERO;
		this.finalAmount = this.amount.subtract(this.discountAmount);
		this.confirmedAt = now;
	}
}
