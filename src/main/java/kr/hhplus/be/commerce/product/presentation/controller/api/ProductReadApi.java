package kr.hhplus.be.commerce.product.presentation.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.product.presentation.response.ProductDetailResponse;

public interface ProductReadApi {
	@Operation(
		tags = "Product",
		summary = "상품 상세 조회"
	)
	@ApiResponseErrorCodes({
		@ApiResponseErrorCode(CommerceCode.NOT_FOUND_PRODUCT)
	})
	ProductDetailResponse readById(
		@Parameter(required = true, description = "상품 고유 식별자") Long productId);
}
