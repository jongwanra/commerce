package kr.hhplus.be.commerce.product.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.exception.CommerceException;
import kr.hhplus.be.commerce.product.domain.repositorty.ProductReader;
import kr.hhplus.be.commerce.product.presentation.response.ProductDetailResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReadByIdQueryManager {
	private final ProductReader productReader;

	@Transactional(readOnly = true)
	public ProductDetailResponse read(Long productId) {
		return productReader.readById(productId)
			.map(ProductDetailResponse::from)
			.orElseThrow(() -> new CommerceException(CommerceCode.NOT_FOUND_PRODUCT));

	}
}
