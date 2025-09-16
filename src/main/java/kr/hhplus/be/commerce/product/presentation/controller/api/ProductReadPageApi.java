package kr.hhplus.be.commerce.product.presentation.controller.api;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import kr.hhplus.be.commerce.global.request.CursorPaginationRequest;
import kr.hhplus.be.commerce.global.response.CursorPage;
import kr.hhplus.be.commerce.product.presentation.request.ProductReadPageRequest;
import kr.hhplus.be.commerce.product.presentation.response.ProductSummaryResponse;

public interface ProductReadPageApi {
	@Operation(
		tags = "Product",
		summary = "상품 목록 페이지네이션 조회"
	)
	CursorPage<ProductSummaryResponse> readPage(
		@ParameterObject CursorPaginationRequest cursorPaginationRequest,
		@ParameterObject ProductReadPageRequest request
	);
}
