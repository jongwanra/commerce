package kr.hhplus.be.commerce.infrastructure.config;

import java.util.concurrent.Executor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;

@TestConfiguration
public class TestAsyncConfig {

	/**
	 * ApplicationPublisher로 발행된 이벤트 처리를 테스트 환경에서 검증하고자 작성했습니다.
	 * 테스트 환경에서는 발행된 이벤트들이 동기적으로 처리됩니다.
	 */
	@Bean
	@Primary
	public Executor asyncExecutor() {
		return new SyncTaskExecutor();
	}
}
