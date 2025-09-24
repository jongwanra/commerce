package kr.hhplus.be.commerce.presentation.global.utils;

import java.util.List;
import java.util.Objects;

import kr.hhplus.be.commerce.domain.global.exception.CommerceException;

public class Validator {
	public static <T> T requireNonNull(Param<T> param) {
		if (Objects.isNull(param.obj)) {
			throw new CommerceException(param.message);
		}

		return param.obj;
	}

	public static <T> T requireNonNull(T obj) {
		if (Objects.isNull(obj)) {
			throw new CommerceException(obj.getClass().getName() + " must not be null");
		}

		return obj;
	}

	public static <T> void requireNonNull(List<Param<T>> params) {
		for (Param<T> param : params) {
			requireNonNull(param);
		}
	}

	public record Param<T>(T obj, String message) {
		public static <T> Param<T> of(T obj) {
			return new Param<>(obj, obj.getClass().getName() + " must not be null");
		}
	}
}
