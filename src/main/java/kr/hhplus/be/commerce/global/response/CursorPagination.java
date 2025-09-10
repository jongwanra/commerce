package kr.hhplus.be.commerce.global.response;

import java.util.List;

public record CursorPagination<E>(
	long totalCount,
	boolean hasNext,
	List<E> items
) {

}
