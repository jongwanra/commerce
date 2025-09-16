package kr.hhplus.be.commerce.product.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.commerce.global.request.CursorPaginationRequest;
import kr.hhplus.be.commerce.global.response.CursorPage;
import kr.hhplus.be.commerce.product.application.ProductReadPageQueryManager;
import kr.hhplus.be.commerce.product.presentation.controller.api.ProductReadPageApi;
import kr.hhplus.be.commerce.product.presentation.request.ProductReadPageRequest;
import kr.hhplus.be.commerce.product.presentation.response.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductReadPageController implements ProductReadPageApi {
	private final ProductReadPageQueryManager productReadPageQueryManager;

	@Override
	@GetMapping("/api/v1/products")
	@ResponseStatus(HttpStatus.OK)
	public CursorPage<ProductSummaryResponse> readPage(
		@Valid CursorPaginationRequest cursorPaginationRequest,
		ProductReadPageRequest request
	) {
		return productReadPageQueryManager.read(request.toQuery(cursorPaginationRequest));
	}
}
