package kr.hhplus.be.commerce.product.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.product.application.ProductReadByIdQueryManager;
import kr.hhplus.be.commerce.product.presentation.controller.api.ProductReadApi;
import kr.hhplus.be.commerce.product.presentation.response.ProductDetailResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductReadController implements ProductReadApi {
	private final ProductReadByIdQueryManager productReadByIdQueryManager;

	@Override
	@GetMapping("/api/v1/product/{productId}")
	@ResponseStatus(HttpStatus.OK)
	public ProductDetailResponse readById(@PathVariable("productId") Long productId) {
		return productReadByIdQueryManager.read(productId);
	}
}
