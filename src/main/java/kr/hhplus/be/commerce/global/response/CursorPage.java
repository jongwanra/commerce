package kr.hhplus.be.commerce.global.response;

import java.util.List;

public record CursorPage<E>(
	long totalCount,
	boolean hasNext,
	List<E> items
) {
	public static <E> CursorPage<E> of(boolean hasNext, long totalCount, List<E> items) {
		return new CursorPage<>(totalCount, hasNext, items);
	}

}
