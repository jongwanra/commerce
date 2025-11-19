package kr.hhplus.be.commerce.presentation.api.product.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductSummaryResponse;

public interface ProductReadAllDailyTopSellingApi {
	@Operation(
		tags = "Product",
		summary = "오늘 판매량 순으로 상품 목록 조회"
	)
	List<ProductSummaryResponse> readAllDailyTopSelling(
		@Parameter(description = "상품 갯수")
		@RequestParam(value = "limit", required = false, defaultValue = "5") @Min(1) @Max(50) Integer limit
	);
}
