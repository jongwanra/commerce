package kr.hhplus.be.commerce.global.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SystemTimeProvider implements TimeProvider {
	@Override
	public LocalDate today() {
		return LocalDate.now();
	}

	@Override
	public LocalDateTime now() {
		return LocalDateTime.now();
	}
}
