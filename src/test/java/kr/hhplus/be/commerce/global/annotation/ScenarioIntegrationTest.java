package kr.hhplus.be.commerce.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

@TestFactory
@Tag(value = "integration")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScenarioIntegrationTest {
	boolean isRunnableInCI() default false;
}
