package kr.hhplus.be.commerce;

import org.springframework.test.util.ReflectionTestUtils;

public abstract class AbstractUnitTestSupport {

	/**
	 * UnitTest 환경에서 Entity에 id값을 강제로 할당합니다.<br/><br/>
	 * Database에 저장하지 않기 때문에 id 값이 자동으로 할당되지 않으므로 ReflectionTestUtils를 사용하여 강제로 할당합니다.
	 *
	 * @param id     할당할 id 값
	 * @param entity id 값을 할당할 Entity 객체
	 *
	 */
	protected void assignId(Long id, Object entity) {
		ReflectionTestUtils.setField(entity, "id", id);
	}
}
