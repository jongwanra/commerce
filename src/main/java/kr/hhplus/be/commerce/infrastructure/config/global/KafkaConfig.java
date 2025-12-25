package kr.hhplus.be.commerce.infrastructure.config.global;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * application.yml에서 이미 Kafka에 대한 설정을 했기 때문에 비워 둡니다.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

}
