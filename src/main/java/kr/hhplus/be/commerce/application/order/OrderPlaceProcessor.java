package kr.hhplus.be.commerce.application.order;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static kr.hhplus.be.commerce.presentation.global.utils.Validator.requireNonNull;
import static kr.hhplus.be.commerce.presentation.global.utils.Validator.*;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.domain.global.exception.CommerceCode;
import kr.hhplus.be.commerce.domain.global.exception.CommerceException;
import kr.hhplus.be.commerce.domain.order.model.Order;
import kr.hhplus.be.commerce.domain.order.model.OrderLine;
import kr.hhplus.be.commerce.domain.order.repository.OrderRepository;
import kr.hhplus.be.commerce.domain.product.model.Product;
import kr.hhplus.be.commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderPlaceProcessor {
	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	@Transactional
	public Output execute(Command command) {
		command.validate();
		List<Long> productIds = command.toProductIds();
		List<Product> products = productRepository.findAllByIdInWithLock(productIds);

		if (products.size() != productIds.size()) {
			throw new CommerceException(CommerceCode.NOT_FOUND_PRODUCT);
		}

		Map<Long, Product> productIdToProductMap = products
			.stream()
			.collect(toMap(Product::getId, product -> product));

		List<Product> productsStockDeducted = command.orderLineCommands()
			.stream()
			.map(orderLineCommand -> {
				Product product = productIdToProductMap.get(orderLineCommand.productId());
				product.deductStock(orderLineCommand.orderQuantity());
				return product;
			})
			.toList();

		List<OrderLine> orderLines = command.orderLineCommands()
			.stream().map(orderLineCommand -> {
				Product product = productIdToProductMap.get(orderLineCommand.productId());
				return OrderLine.place(product.getId(), product.getName(), product.getPrice(),
					orderLineCommand.orderQuantity());
			}).toList();

		return new Output(
			productRepository.saveAll(productsStockDeducted),
			orderRepository.save(Order.place(command.userId, orderLines))
		);
	}

	public record Command(
		Long userId,
		List<OrderLineCommand> orderLineCommands
	) {
		public void validate() {
			requireNonNull(List.of(Param.of(userId), Param.of(orderLineCommands)));
			if (orderLineCommands.isEmpty()) {
				throw new CommerceException(CommerceCode.ORDER_LINE_COMMANDS_IS_EMPTY);
			}
			orderLineCommands
				.forEach(it -> {
					if (isNull(it.orderQuantity()) || it.orderQuantity() <= 0) {
						throw new CommerceException(CommerceCode.ORDER_QUANTITY_MUST_BE_POSITIVE);
					}
				});
		}

		public List<Long> toProductIds() {
			return orderLineCommands.stream()
				.map(OrderLineCommand::productId)
				.toList();
		}
	}

	public record OrderLineCommand(
		Long productId,
		Integer orderQuantity
	) {
	}

	public record Output(
		List<Product> products,
		Order order
	) {
	}
}
