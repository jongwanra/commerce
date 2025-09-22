package kr.hhplus.be.commerce.global.open_api.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import kr.hhplus.be.commerce.global.exception.CommerceCode;

@Target({METHOD, ANNOTATION_TYPE, FIELD})
@Retention(value = RUNTIME)
@Inherited
public @interface ApiResponseErrorCode {
	CommerceCode value();

}
