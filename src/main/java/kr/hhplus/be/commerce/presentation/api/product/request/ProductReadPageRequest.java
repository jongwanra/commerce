package kr.hhplus.be.commerce.presentation.api.product.request;

import static kr.hhplus.be.commerce.application.product.ProductReadPageQueryManager.*;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.commerce.domain.product.repository.input.enums.ProductSortType;
import kr.hhplus.be.commerce.presentation.global.request.CursorPaginationRequest;

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
