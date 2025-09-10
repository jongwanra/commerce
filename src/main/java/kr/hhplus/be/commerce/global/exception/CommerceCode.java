package kr.hhplus.be.commerce.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommerceCode {
	SUCCESS(200, "CM-0000", "정상 처리되었습니다."),
	CREATED(201, "CM-0001", "정상 생성되었습니다."),
	UNKNOWN_ERROR(500, "CM-0002", "알 수 없는 오류가 발생했습니다. 관리자에게 문의하세요."),
	BAD_REQUEST(400, "CM-0003", "%s"),

	// Auth(AU-0001 ~)
	UNAUTHENTICATED_USER(401, "AU-0001", "인증되지 않은 사용자입니다."),

	// User(UR-0001 ~)
	NOT_FOUND_USER(400, "UR-0001", "존재하지 않는 사용자입니다."),

	// Cash(CH-0001 ~)
	NOT_FOUND_CASH(400, "CH-0001", "사용자의 잔액 정보가 존재하지 않습니다."),
	CHARGE_AMOUNT_MUST_BE_POSITIVE(400, "CH-0002", "충전 금액은 0원보다 커야 합니다."),
	CHARGE_AMOUNT_PER_ONCE_EXCEEDS_LIMIT(400, "CH-0003", "한 번에 %s원을 초과하여 충전할 수 없습니다."),

	// Coupon(CP-0001 ~)
	NOT_FOUND_COUPON(400, "CP-0001", "존재하지 않는 쿠폰입니다."),
	EXPIRED_COUPON(400, "CP-0002", "만료된 쿠폰입니다."),
	OUT_OF_STOCK_COUPON(400, "CP-0003", "재고가 소진된 쿠폰입니다."),
	ALREADY_ISSUED_COUPON(400, "CP-0004", "이미 발급 받은 쿠폰입니다."),
	;

	private final int status;
	private final String code;
	private final String message;

}
