package kr.hhplus.be.commerce.global.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

// @Tag를 사용하여, 이후 CI/CD 파이프라인에서 무거운 통합 테스트를 제외할 수 있도록 합니다.
@Test
@Tag(value = "integration")
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {
}
