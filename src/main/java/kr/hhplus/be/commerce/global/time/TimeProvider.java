package kr.hhplus.be.commerce.global.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeProvider {
	/**
	 * 현재 날짜를 반환합니다.
	 */
	LocalDate today();

	/**
	 * 현재 날짜/시간을 반환합니다.
	 */
	LocalDateTime now();
}
