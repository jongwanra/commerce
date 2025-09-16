package kr.hhplus.be.commerce.coupon.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.commerce.coupon.persistence.UserCouponRepository;
import kr.hhplus.be.commerce.coupon.persistence.entity.UserCouponEntity;
import kr.hhplus.be.commerce.coupon.presentation.response.UserCouponSummaryResponse;
import kr.hhplus.be.commerce.global.response.CursorPage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCouponReadPageQueryManager {
	private final UserCouponRepository userCouponRepository;

	@Transactional(readOnly = true)
	public CursorPage<UserCouponSummaryResponse> read(Query query) {
		Pageable pageable = PageRequest.ofSize(query.size);
		Page<UserCouponEntity> pageOfUserCoupon = userCouponRepository.findPageByUserId(
			query.userId(),
			query.lastId() == 0L ? Long.MAX_VALUE : query.lastId(),
			pageable
		);

		return new CursorPage<>(
			pageOfUserCoupon.getTotalElements(),
			pageOfUserCoupon.hasNext(),
			pageOfUserCoupon
				.getContent()
				.stream()
				.map(userCoupon -> new UserCouponSummaryResponse(
					userCoupon.getId(),
					userCoupon.getCouponId(),
					userCoupon.getOriginName(),
					userCoupon.getOriginDiscountType(),
					userCoupon.getOriginDiscountAmount(),
					userCoupon.getStatus(),
					userCoupon.getIssuedAt(),
					userCoupon.getOriginExpiredAt()
				))
				.toList()
		);
	}

	public record Query(
		Long userId,
		Long lastId,
		Integer size
	) {
	}
}
