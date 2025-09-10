package kr.hhplus.be.commerce.global.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record CursorPaginationRequest(
	@Schema(description = "조회 목록 중, 마지막 항목의 고유 식별자, 첫 페이지 호출 시: 0", example = "0")
	@NotNull(message = "lastId는 필수값 입니다.")
	Long lastId,

	@Max(value = 50, message = "pageSize는 50 이하로 설정해주세요.")
	@NotNull(message = "size는 필수값 입니다.")
	@Schema(description = "페이지 크기, 최대 50", example = "10", maximum = "50", minimum = "1")
	Integer size
) {
}
