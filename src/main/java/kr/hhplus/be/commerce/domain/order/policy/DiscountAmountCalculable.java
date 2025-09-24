package kr.hhplus.be.commerce.domain.order.policy;

import java.math.BigDecimal;

@FunctionalInterface
public interface DiscountAmountCalculable {
	BigDecimal calculateDiscountAmount(BigDecimal amount);
}
