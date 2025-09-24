package kr.hhplus.be.commerce.application.order;

import static kr.hhplus.be.commerce.application.order.OrderPlaceProcessor.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.domain.order.model.enums.OrderStatus;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class OrderPlaceProcessorUnitTest {
	@InjectMocks
	private OrderPlaceProcessor orderPlaceProcessor;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductRepository productRepository;

	// 작성 이유: 주문하고자 하는 상품이 존재하지 않는 상품인 경우 예외를 발생시키는지 검증하기 위해 작성했습니다.
	@Test
	void 주문할_상품이_존재하지_않는_경우_예외를_발생_시킨다() {
		// given
		Long notExistProductId = 999L;
		Command command = new Command(1L, List.of(new OrderLineCommand(notExistProductId, 1)));

		// mock
		given(productRepository.findAllByIdInWithLock(List.of(notExistProductId)))
			.willReturn(List.of());

		// when & then
		assertThatThrownBy(() -> orderPlaceProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("존재하지 않는 상품입니다.");
	}

	// 작성 이유: 주문할 상품의 재고가 부족한 경우 예외를 발생시키는지 검증하기 위해 작성했습니다.
	// [경계값: 재고: 1, 주문 수량:2]
	@Test
	void 주문할_상품의_재고가_주문_수량보다_부족한_경우_예외를_발생_시킨다() {
		// given
		Long productId = 999L;
		Long userId = 1L;
		final int remainStock = 1;
		final int orderQuantity = 2; // 재고보다 많은 수량 주문

		Product product = Product.builder()
			.name("product name")
			.price(BigDecimal.valueOf(10_000))
			.stock(remainStock) // 재고 1
			.build();
		product.assignId(productId);

		Command command = new Command(userId, List.of(new OrderLineCommand(productId, orderQuantity)));

		// mock
		given(productRepository.findAllByIdInWithLock(List.of(productId)))
			.willReturn(List.of(product));

		// when & then
		assertThatThrownBy(() -> orderPlaceProcessor.execute(command))
			.isInstanceOf(CommerceException.class)
			.hasMessage("상품의 재고가 부족합니다.");
	}

	// 작성 이유: 주문할 상품의 재고와 주문 수량이 같은 경우 주문이 가능 한지 확인하기 위해 작성했습니다.
	// [경계값: 재고: 1, 주문 수량:1]
	@Test
	void 주문할_상품의_재고가_주문_수량과_일치한_경우_주문할_수_있다() {
		// given
		Long productId = 999L;
		Long userId = 1L;
		final int stock = 1;
		final int orderQuantity = 1; // 재고와 동일한 주문 수량
		Long orderId = 1L;
		Long orderLineId = 1L;

		Product product = Product.builder()
			.name("product name")
			.price(BigDecimal.valueOf(10_000))
			.stock(stock)
			.build();
		product.assignId(productId);

		Command command = new Command(userId, List.of(new OrderLineCommand(productId, orderQuantity)));
		// mock
		given(productRepository.findAllByIdInWithLock(List.of(productId)))
			.willReturn(List.of(product));

		Product savedProduct = Product.builder()
			.name("product name")
			.price(BigDecimal.valueOf(10_000))
			.stock(stock - orderQuantity)
			.build();
		savedProduct.assignId(productId);

		given(productRepository.saveAll(any()))
			.willReturn(List.of(savedProduct));

		OrderLine savedOrderLine = OrderLine.restore(
			orderLineId,
			orderId,
			productId,
			"product name",
			BigDecimal.valueOf(10_000),
			orderQuantity
		);

		Order savedOrder = Order.restore(
			orderId,
			userId,
			OrderStatus.PENDING,
			BigDecimal.valueOf(10_000),
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			List.of(savedOrderLine),
			null
		);

		given(orderRepository.save(any()))
			.willReturn(savedOrder);
		// when
		Output output = orderPlaceProcessor.execute(command);

		// then
		List<Product> products = output.products();
		assertThat(products.size()).isOne();
		assertThat(products.get(0).getStock()).isZero();
		assertThat(products.get(0).getId()).isEqualTo(productId);
		assertThat(products.get(0).getName()).isEqualTo("product name");
		assertThat(products.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(10_000));

		Order order = output.order();
		assertThat(order.getUserId()).isEqualTo(userId);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
		assertThat(order.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
		assertThat(order.getId()).isEqualTo(1L);
		assertThat(order.getOrderLines().size()).isOne();

		OrderLine orderLine = order.getOrderLines().get(0);
		assertThat(orderLine.getId()).isEqualTo(1L);
		assertThat(orderLine.getProductId()).isEqualTo(productId);
		assertThat(orderLine.getProductName()).isEqualTo("product name");
		assertThat(orderLine.getProductAmount()).isEqualByComparingTo(BigDecimal.valueOf(10_000));
		assertThat(orderLine.getOrderQuantity()).isEqualTo(1);

	}

	// 작성 이유: 주문할 상품의 수량이 양수가 아닌 경우 예외를 발생시키는지 검증 [경계값: 0]
	@Test
	void 주문할_상품의_수량이_양수가_아닌_경우_예외를_발생_시킨다() {
		// given
		final int zeroOrderQuantity = 0;
		List<OrderLineCommand> orderLineCommands = List.of(new OrderLineCommand(1L, zeroOrderQuantity));
		Command command = new Command(1L, orderLineCommands);

		// when & then
		assertThatThrownBy(() -> {
			orderPlaceProcessor.execute(command);
		})
			.isInstanceOf(CommerceException.class)
			.hasMessage("주문 수량은 1개 이상이어야 합니다.");
	}

}
