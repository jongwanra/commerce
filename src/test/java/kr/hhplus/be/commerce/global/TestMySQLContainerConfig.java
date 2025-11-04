package kr.hhplus.be.commerce.global;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PreDestroy;

@Configuration
class TestMySQLContainerConfig {
	public final static MySQLContainer<?> MYSQL_CONTAINER;

	static {
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
			.withDatabaseName("hhplus")
			.withUsername("application")
			.withPassword("application")
			.withInitScript("sql/schema.sql");
		MYSQL_CONTAINER.start();

		System.setProperty("spring.datasource.url",
			MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
	}

	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}
	}

}
