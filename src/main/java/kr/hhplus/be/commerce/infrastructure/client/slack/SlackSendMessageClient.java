package kr.hhplus.be.commerce.infrastructure.client.slack;

/**
 * 외부 API 연동 예시로 Slack에 메세지를 전송하는 기능을 생각했습니다.
 */
public interface SlackSendMessageClient {
	void send(String message);
}
