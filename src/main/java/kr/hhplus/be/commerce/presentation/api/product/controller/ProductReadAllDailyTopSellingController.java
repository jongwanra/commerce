package kr.hhplus.be.commerce.presentation.api.product.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.application.product.ProductReadAllDailyTopSellingQueryManager;
import kr.hhplus.be.commerce.presentation.api.product.controller.api.ProductReadAllDailyTopSellingApi;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductReadAllDailyTopSellingController implements ProductReadAllDailyTopSellingApi {
	private final ProductReadAllDailyTopSellingQueryManager productReadAllDailyTopSellingQueryManager;

	@Override
	@GetMapping("/api/v1/products/top-selling/daily")
	@ResponseStatus(HttpStatus.OK)
	public List<ProductSummaryResponse> readAllDailyTopSelling(Integer limit) {
		LocalDate today = LocalDate.now();

		return productReadAllDailyTopSellingQueryManager.read(new ProductReadAllDailyTopSellingQueryManager.Query(
			today,
			limit
		));
	}
}
