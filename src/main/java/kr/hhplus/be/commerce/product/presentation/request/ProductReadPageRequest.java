package kr.hhplus.be.commerce.product.presentation.request;

import static kr.hhplus.be.commerce.product.application.ProductReadPageQueryManager.*;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.commerce.global.request.CursorPaginationRequest;
import kr.hhplus.be.commerce.product.domain.repositorty.input.enums.ProductSortType;

public record ProductReadPageRequest(
	@Schema(description = """
		- 정렬 기준 (기본값: newest)
			- newest: 최신순
			- oldest: 오래된순
		""", example = "newest", nullable = true)
	String sort
) {
	public Query toQuery(CursorPaginationRequest cursorPaginationRequest) {
		return new Query(
			cursorPaginationRequest.lastId(),
			cursorPaginationRequest.size(),
			ProductSortType.from(sort)
		);
	}
}
