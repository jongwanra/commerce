package kr.hhplus.be.commerce.order.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderLine {
	private Long id;
	private Long orderId;
	private Long productId;
	private String originProductName;
	private BigDecimal originProductAmount;
	private Integer orderQuantity;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
