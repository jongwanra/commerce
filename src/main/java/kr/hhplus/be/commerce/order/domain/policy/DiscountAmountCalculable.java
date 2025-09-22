package kr.hhplus.be.commerce.order.domain.policy;

import java.math.BigDecimal;

@FunctionalInterface
public interface DiscountAmountCalculable {
	BigDecimal calculateDiscountAmount(BigDecimal amount);
}
