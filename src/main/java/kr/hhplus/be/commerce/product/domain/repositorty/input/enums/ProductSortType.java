package kr.hhplus.be.commerce.product.domain.repositorty.input.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductSortType {
	NEWEST("최신순"),
	OLDEST("오래된순"),
	;

	private final String description;

	public static ProductSortType from(String sort) {
		return Arrays.stream(values())
			.filter(it -> it.name().equalsIgnoreCase(sort))
			.findFirst()
			.orElse(NEWEST);
	}
}
