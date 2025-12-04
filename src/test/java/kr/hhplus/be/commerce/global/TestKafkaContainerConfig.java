package kr.hhplus.be.commerce.global;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PreDestroy;

@Configuration
public class TestKafkaContainerConfig {
	private final static KafkaContainer KAFKA_CONTAINER;

	static {
		/**
		 * Kafka에 어떤 Broker에 연결해도 자동으로 클러스터에서 처리하기 때문에 무방합니다.
		 */
		DockerImageName kafkaDockerImageName = DockerImageName.parse("apache/kafka:3.8.1");
		KAFKA_CONTAINER = new KafkaContainer(kafkaDockerImageName);

		KAFKA_CONTAINER.setPortBindings(List.of("9092:9092"));
		KAFKA_CONTAINER.start();

		System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());
	}

	@PreDestroy
	public void preDestroy() {
		if (KAFKA_CONTAINER.isRunning()) {
			KAFKA_CONTAINER.stop();
		}
	}

}
