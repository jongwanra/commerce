package kr.hhplus.be.commerce.global.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FixedTimeProvider implements TimeProvider {
	private final LocalDateTime fixedDateTime;

	private FixedTimeProvider(final LocalDateTime fixedDateTime) {
		this.fixedDateTime = fixedDateTime;
	}

	private FixedTimeProvider(final LocalDate fixedDate) {
		this.fixedDateTime = fixedDate.atStartOfDay();
	}

	public static FixedTimeProvider of(LocalDateTime fixedDateTime) {
		return new FixedTimeProvider(fixedDateTime);
	}

	public static FixedTimeProvider of(LocalDate fixedDate) {
		return new FixedTimeProvider(fixedDate);
	}

	@Override
	public LocalDate today() {
		return fixedDateTime.toLocalDate();
	}

	@Override
	public LocalDateTime now() {
		return fixedDateTime;
	}
}
