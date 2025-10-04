package kr.hhplus.be.commerce.infrastructure.client.slack;

public class SlackException extends RuntimeException {
	public SlackException(String message) {
		super(message);
	}
}
