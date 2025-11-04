package kr.hhplus.be.commerce.global;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import kr.hhplus.be.commerce.global.annotation.IntegrationTest;

@Configuration
public class RedisConfigTest extends AbstractIntegrationTestSupport {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@DisplayName("레디스 컨테이너 테스트")
	@IntegrationTest
	void contextsLoads() {
		redisTemplate.opsForValue().set("key", "value");
		String result = redisTemplate.opsForValue().get("key");
		assertThat(result).isEqualTo("value");
	}
}
