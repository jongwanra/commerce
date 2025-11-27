package kr.hhplus.be.commerce.infrastructure.client.slack;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SlackSendMessageClientImpl implements SlackSendMessageClient {
	@Override
	public void send(String message) {
		// TODO Impl.
		log.info("[슬랙 메세지 발송 성공: {}", message);
	}
}
