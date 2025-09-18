package kr.hhplus.be.commerce.global.config;

import static java.util.Objects.*;

import java.util.Arrays;
import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import kr.hhplus.be.commerce.global.exception.CommerceCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCode;
import kr.hhplus.be.commerce.global.open_api.annotation.ApiResponseErrorCodes;
import kr.hhplus.be.commerce.global.response.EmptyResponse;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OpenApiConfig {
	@Bean
	public GroupedOpenApi groupedOpenApi() {
		return GroupedOpenApi.builder()
			.group("Commerce")
			.addOperationCustomizer(getOperationCustomizer())
			.pathsToMatch("/api/**")
			.addOpenApiCustomizer(openApi -> {
				openApi.info(new Info()
					.title("Commerce API Sepecifiaction")
					.version("v1")
					.description("""
						## 공유 사항
							1. Database + Cookie 기반으로 인증을 처리합니다. (TODO)
							2. 일시는 전부 `UTC`로 통일합니다.
						""")
				);
				openApi.servers(List.of(new Server().url("http://localhost:8080")));
			})
			.build();
	}

	private OperationCustomizer getOperationCustomizer() {
		return (operation, handlerMethod) -> {
			handleApiResponses(operation, handlerMethod);
			return operation;
		};
	}

	private void handleApiResponses(Operation operation, HandlerMethod handlerMethod) {
		ApiResponses apiResponses = operation.getResponses();
		CommerceCode commerceCode = getCommerceCode(operation.getResponses());
		if (isNull(apiResponses) || isNull(commerceCode)) {
			return;
		}
		removeNotUsedApiResponses(apiResponses);
		wrapAsCommerceResponse(operation, commerceCode);
		handleErrorApiResponses(handlerMethod, apiResponses);
	}

	private void removeNotUsedApiResponses(ApiResponses apiResponses) {
		apiResponses.remove("400");
		apiResponses.remove("401");
		apiResponses.remove("403");
		apiResponses.remove("404");
		apiResponses.remove("500");
	}

	private void wrapAsCommerceResponse(Operation operation, CommerceCode commerceCode) {
		ApiResponse apiResponse = getApiResponse(operation.getResponses());
		if (isNull(apiResponse)) {
			return;
		}
		final Content content = apiResponse.getContent();
		content.forEach((mediaTypeKey, mediaType) -> {
			Schema<?> originalSchema = mediaType.getSchema();
			Schema<?> wrappedSchema = wrapSchema(originalSchema, commerceCode);
			mediaType.setSchema(wrappedSchema);
		});

	}

	/**
	 * 성공 케이스에 대해서는 200, 201만 반환합니다.
	 */
	private ApiResponse getApiResponse(ApiResponses apiResponses) {
		if (apiResponses.containsKey("200")) {
			return apiResponses.get("200");
		}
		if (apiResponses.containsKey("201")) {
			return apiResponses.get("201");
		}
		return null;
	}

	private CommerceCode getCommerceCode(ApiResponses apiResponses) {
		if (apiResponses.containsKey("200")) {
			return CommerceCode.SUCCESS;
		}
		if (apiResponses.containsKey("201")) {
			return CommerceCode.CREATED;
		}
		return null;
	}

	private void handleErrorApiResponses(HandlerMethod handlerMethod, ApiResponses apiResponses) {
		ApiResponseErrorCodes apiResponseCodesAnnotation = handlerMethod.getMethodAnnotation(
			ApiResponseErrorCodes.class);

		if (isNull(apiResponseCodesAnnotation)) {
			return;
		}

		ApiResponseErrorCode[] apiResponseErrorCodes = apiResponseCodesAnnotation.value();
		Arrays.stream(apiResponseErrorCodes).forEach(
			apiResponseErrorCode -> {
				CommerceCode commerceCode = apiResponseErrorCode.value();
				ApiResponse newApiResponse = new ApiResponse();

				newApiResponse.setDescription(commerceCode.getMessage());
				newApiResponse.setContent(new Content().addMediaType("application/json",
					new io.swagger.v3.oas.models.media.MediaType().schema(wrapErrorSchema(commerceCode))));
				final String code = commerceCode.getStatus() + "\n(" + commerceCode.getCode() + ")";
				apiResponses.put(code, newApiResponse);
			}
		);

	}

	private Schema<?> wrapSchema(Schema<?> originalSchema, CommerceCode commerceCode) {
		final Schema<?> wrapperSchema = new Schema<>();
		wrapperSchema.addProperty("success", new Schema<>().type("boolean").example(true));
		wrapperSchema.addProperty("code", new Schema<>().type("string").example(commerceCode.getCode()));
		wrapperSchema.addProperty("message", new Schema<>().type("string").example(commerceCode.getMessage()));
		wrapperSchema.addProperty("data", originalSchema);

		return wrapperSchema;
	}

	private Schema<?> wrapErrorSchema(CommerceCode commerceCode) {
		final Schema<?> wrapperSchema = new Schema<>();

		wrapperSchema.addProperty("success", new Schema<>().type("boolean").example(false));
		wrapperSchema.addProperty("code", new Schema<>().type("string").example(commerceCode.getCode()));
		wrapperSchema.addProperty("message", new Schema<>().type("string").example(commerceCode.getMessage()));
		wrapperSchema.addProperty("data", new Schema<>().type("string").example(EmptyResponse.INSTANCE));

		return wrapperSchema;
	}
}
