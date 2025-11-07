package kr.hhplus.be.commerce.infrastructure.global.lock;

import java.util.stream.IntStream;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Expression Language Parser
 */
@Slf4j
public class CommerceSpringELParser {

	public static Object parse(String[] parameterNames, Object[] args, String key) {
		log.info("parameterNames = {}", parameterNames);
		log.info("args = {}", args);
		log.info("key = {}", key);
		SpelExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext();

		IntStream.range(0, parameterNames.length)
			.forEach((index) -> context.setVariable(parameterNames[index], args[index]));

		return parser.parseExpression(key).getValue(context, Object.class);
	}

}
