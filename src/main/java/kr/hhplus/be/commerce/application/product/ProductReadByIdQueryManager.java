package kr.hhplus.be.commerce.application.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.product.repository.ProductReader;
import kr.hhplus.be.commerce.presentation.api.product.response.ProductDetailResponse;
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
