package kr.hhplus.be.commerce.presentation.api.order.controller;

import static kr.hhplus.be.commerce.presentation.api.order.request.OrderPlaceRequest.*;
import static kr.hhplus.be.commerce.presentation.global.utils.CommerceHttpRequestHeaderName.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;
import kr.hhplus.be.commerce.infrastructure.persistence.cash.entity.CashEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.payment.entity.PaymentEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.product.entity.ProductEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.UserEntity;
import kr.hhplus.be.commerce.infrastructure.persistence.user.entity.enums.UserStatus;
import kr.hhplus.be.commerce.presentation.api.order.request.OrderPlaceRequest;
import kr.hhplus.be.commerce.presentation.global.resolver.LoginUserIdArgumentResolver;
import kr.hhplus.be.commerce.presentation.global.response.CommerceResponse;
import kr.hhplus.be.commerce.presentation.global.response.EmptyResponse;

class OrderPlaceControllerIntegrationTest extends AbstractIntegrationTestSupport {
	private RestClient restClient;

	@MockitoBean
	private LoginUserIdArgumentResolver loginUserIdArgumentResolver;

	@LocalServerPort
	private int port;

	@BeforeEach
	public void setup() {
		restClient = RestClient.builder()
			.baseUrl("http://localhost:" + port)
			.build();
	}

	/**
	 * 작성 이유: 동일한 멱등키로 중복 요청을 할 경우, 아래 케이스를 만족하는지 확인하기 위해 작성했습니다.
	 * 1. 동일한 결과값을 반환하는지 확인
	 * 2. 중복 결제 되지 않음.
	 */
	@IntegrationTest
	void 동일한_멱등키로_중복_요청을_했을_경우_동일한_결과값을_반환한다() {
		// given
		UserEntity user = userJpaRepository.save(UserEntity.builder()
			.email("user@gmail.com")
			.encryptedPassword("encrypted_password")
			.status(UserStatus.ACTIVE)
			.build());
		Long userId = user.getId();
		cashJpaRepository.save(CashEntity.builder()
			.balance(BigDecimal.valueOf(15_000))
			.userId(userId)
			.build());

		Product product = productJpaRepository.save(ProductEntity.builder()
			.name("오뚜기 진라면 매운맛 120g")
			.price(BigDecimal.valueOf(6_700))
			.stock(100)
			.build()).toDomain();

		final String idempotencyKey = UUID.randomUUID().toString();
		final List<OrderLineRequest> orderLineRequests = List.of(
			new OrderLineRequest(
				product.id(),
				1
			)
		);
		OrderPlaceRequest orderPlaceRequest = new OrderPlaceRequest(null, BigDecimal.valueOf(6_700), orderLineRequests);

		// mock
		given(loginUserIdArgumentResolver.supportsParameter(any())).willReturn(true);
		given(loginUserIdArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(userId);

		CommerceResponse<EmptyResponse> originalResponse = restClient.post()
			.uri("/api/v1/me/orders")
			.headers((httpHeaders -> {
				httpHeaders.set(X_COMMERCE_IDEMPOTENCY_KEY, idempotencyKey);
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			}))
			.body(orderPlaceRequest)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		// when
		CommerceResponse<EmptyResponse> retriedResponse = restClient.post()
			.uri("/api/v1/me/orders")
			.headers((httpHeaders -> {
				httpHeaders.set(X_COMMERCE_IDEMPOTENCY_KEY, idempotencyKey);
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			}))
			.body(orderPlaceRequest)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		assertThat(originalResponse).isEqualTo(retriedResponse).as("동일한 결과값을 반환하는지 확인합니다.");

		// 중복 결제가 발생하지 않았는지 확인합니다.
		List<PaymentEntity> paymentEntities = paymentJpaRepository.findAll();
		assertThat(paymentEntities.size()).isOne();
	}

}
