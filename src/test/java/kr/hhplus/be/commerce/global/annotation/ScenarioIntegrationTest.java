package kr.hhplus.be.commerce.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.context.jdbc.Sql;

@TestFactory
@Tag(value = "integration")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public @interface ScenarioIntegrationTest {
	boolean isRunnableInCI() default false;
}
