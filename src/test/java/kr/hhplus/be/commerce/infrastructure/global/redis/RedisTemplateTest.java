package kr.hhplus.be.commerce.infrastructure.global.redis;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import kr.hhplus.be.commerce.global.AbstractIntegrationTestSupport;
import kr.hhplus.be.commerce.global.annotation.IntegrationTest;

public class RedisTemplateTest extends AbstractIntegrationTestSupport {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@IntegrationTest
	public void redisTemplate에_키_값을_넣고_조회하고_삭제할_수_있다() {
		redisTemplate.opsForValue().set("test-key", "test-value");
		String result = redisTemplate.opsForValue().get("test-key");
		

		assertThat(result).isEqualTo("test-value");

	}

	
	@IntegrationTest
	public void redisTemplate에_스코어_기반_정렬된_셋을_넣고_조회할_수_있다() {
		redisTemplate.opsForZSet().add("sorted_set_key", "1", 10);
		redisTemplate.opsForZSet().add("sorted_set_key", "2", 11);
		redisTemplate.opsForZSet().add("sorted_set_key", "3", 12);

		Set<String> result = redisTemplate.opsForZSet().range("sorted_set_key", 0, -1);
		assertThat(result).containsExactly("1", "2", "3");
	}
}
