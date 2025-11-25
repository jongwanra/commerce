package kr.hhplus.be.commerce.global;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;

import jakarta.annotation.PreDestroy;

@Configuration
public class TestRedisContainerConfig {
	public final static GenericContainer<?> REDIS_CONTAINER;

	static {
		REDIS_CONTAINER = new GenericContainer<>("redis:7.0")
			.withExposedPorts(6379);
		REDIS_CONTAINER.start();
		final String host = REDIS_CONTAINER.getHost();
		final Integer port = REDIS_CONTAINER.getMappedPort(6379);

		System.setProperty("spring.data.redis.host", host);
		System.setProperty("spring.data.redis.port", port.toString());
	}

	@PreDestroy
	public void preDestroy() {
		if (REDIS_CONTAINER.isRunning()) {
			REDIS_CONTAINER.stop();
		}
	}

}
