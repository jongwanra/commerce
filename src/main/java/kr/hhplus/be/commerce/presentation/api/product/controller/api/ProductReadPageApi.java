package kr.hhplus.be.commerce.presentation.api.product.controller.api;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import kr.hhplus.be.commerce.presentation.api.product.request.ProductReadPageRequest;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductSummaryResponse;
import kr.hhplus.be.commerce.presentation.global.request.CursorPaginationRequest;
import kr.hhplus.be.commerce.presentation.global.response.CursorPage;

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
