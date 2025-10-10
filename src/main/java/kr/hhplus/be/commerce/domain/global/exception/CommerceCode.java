package kr.hhplus.be.commerce.domain.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommerceCode {
	SUCCESS(200, "CM-0000", "정상 처리되었습니다."),
	CREATED(201, "CM-0001", "정상 생성되었습니다."),
	UNKNOWN_ERROR(500, "CM-0002", "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요."),
	BAD_REQUEST(400, "CM-0003", "%s"),
	IDEMPOTENCY_KEY_IS_REQUIRED(400, "CM-0004", "멱등키는 필수값입니다."),

	// Auth(AU-0001 ~)
	UNAUTHENTICATED_USER(401, "AU-0001", "인증되지 않은 사용자입니다."),

	UNAUTHORIZED_USER(403, "AU-0002", "접근 권한이 없는 사용자입니다."),

	// User(UR-0001 ~)
	NOT_FOUND_USER(400, "UR-0001", "존재하지 않는 사용자입니다."),

	// Cash(CH-0001 ~)
	NOT_FOUND_CASH(400, "CH-0001", "사용자의 잔액 정보가 존재하지 않습니다."),

	CHARGE_AMOUNT_MUST_BE_POSITIVE(400, "CH-0002", "충전 금액은 0원보다 커야 합니다."),

	CHARGE_AMOUNT_PER_ONCE_EXCEEDS_LIMIT(400, "CH-0003", "한 번에 %s원을 초과하여 충전할 수 없습니다."),

	INSUFFICIENT_CASH(400, "CH-0004", "잔액이 부족합니다. 잔액을 충전해주세요."),

	AMOUNT_MUST_BE_POSITIVE(400, "CH-0005", "금액은 0원보다 커야 합니다."),

	// Coupon(CP-0001 ~)
	NOT_FOUND_COUPON(400, "CP-0001", "존재하지 않는 쿠폰입니다."),

	EXPIRED_COUPON(400, "CP-0002", "만료된 쿠폰입니다."),

	OUT_OF_STOCK_COUPON(400, "CP-0003", "재고가 소진된 쿠폰입니다."),

	ALREADY_ISSUED_COUPON(400, "CP-0004", "이미 발급 받은 쿠폰입니다."),

	// UserCoupon(UC-0001 ~)
	UNAVAILABLE_USER_COUPON(400, "UC-0001", "이용 불가능한 쿠폰입니다."),

	NOT_FOUND_USER_COUPON(400, "UC-0002", "존재하지 않는 사용자 쿠폰입니다."),

	// Order(OR-0001 ~)
	NOT_FOUND_ORDER(400, "OR-0001", "존재하지 않는 주문입니다."),

	NOT_FOUND_ORDER_LINE(400, "OR-0002", "존재하지 않는 주문 상품입니다."),

	ORDER_LINE_COMMANDS_IS_EMPTY(400, "OR-0003", "주문할 상품은 최소 1개 이상이어야 합니다."),

	ORDER_QUANTITY_MUST_BE_POSITIVE(400, "OR-0004", "주문 수량은 1개 이상이어야 합니다."),

	ALREADY_CONFIRMED_ORDER(400, "OR-0005", "이미 확정된 주문입니다."),

	// Product(PT-0001 ~)
	NOT_FOUND_PRODUCT(400, "PT-0001", "존재하지 않는 상품입니다."),

	INSUFFICIENT_PRODUCT_STOCK(400, "PT-0002", "상품의 재고가 부족합니다."),

	// Payment(PY-0001 ~)
	MISMATCHED_EXPECTED_AMOUNT(400, "PY-0001", "예상 결제 금액과 실제 결제 금액이 일치하지 않습니다."),
	;

	private final int status;
	private final String code;
	private final String message;

}
