package kr.hhplus.be.commerce.infrastructure.config.global;

import static java.util.Objects.*;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

	private final RedisProperties redisProperties;

	// 1. 커넥션 풀 설정 (성능 핵심: 200개 유지)
	@Bean
	@Primary // Redisson 라이브러리와 충돌 위험이 있습니다.
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(redisProperties.getHost());
		redisConfig.setPort(redisProperties.getPort());
		redisConfig.setDatabase(redisProperties.getDatabase());

		if (nonNull(redisProperties.getPassword())) {
			redisConfig.setPassword(redisProperties.getPassword());
		}

		GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setMaxTotal(200);
		poolConfig.setMaxIdle(200);
		poolConfig.setMinIdle(50);
		poolConfig.setMaxWait(Duration.ofSeconds(3));

		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
			.commandTimeout(Duration.ofSeconds(10))
			.poolConfig(poolConfig)
			.build();

		return new LettuceConnectionFactory(redisConfig, clientConfig);
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate() {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());

		// String Serializer 설정 (Lua Script 호환)
		StringRedisSerializer serializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(serializer);
		redisTemplate.setValueSerializer(serializer);
		redisTemplate.setHashKeySerializer(serializer);
		redisTemplate.setHashValueSerializer(serializer);

		return redisTemplate;
	}
}
