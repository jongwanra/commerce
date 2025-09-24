package kr.hhplus.be.commerce.presentation.api.product.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductDetailResponse;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.presentation.global.open_api.annotation.ApiResponseErrorCodes;

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
