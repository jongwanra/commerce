# Kafka Study

## Kafka란 무엇인가?

- **Kafka**는 대규모 실시간 이벤트 데이터를 발행/구독하고, 영구 저장하여 재처리할 수 있는 분산 이벤트 스트리밍 플랫폼이다.

## Kafka의 핵심 특징

### 1. 높은 처리량 (High Throughput)

- 초당 수백만 건의 메시지 처리 가능
- 배치 처리와 압축으로 네트워크 효율성 극대화
- 순차적 디스크 I/O로 빠른 쓰기/읽기 가능

### 2. 영구 저장 (Persistence)

- 모든 메시지를 디스크에 저장함
- 설정한 보관 기간(Retention period) 동안 데이터 유지
- 컨슈머 장애 시에도 데이터 손실 없음

### 3. 확장성 (Scalability)

- 브로커, 파티션 추가로 수평 확장 가능
- 데이터와 처리를 여러 노드에 분산
- 클러스터 재시작 없이 확장 가능

### 4. 내결함성 (Fault Tolerance)

- 데이터 복제(Replication)를 통한 안정성 보장
- 브로커 장애 시 자동 Failover
- 리더 파티션 장애 시 팔로워가 리더로 승격

### 5. 순서 보장 (Ordering)

- 같은 파티션 내에서 메시지 순서 보장
- 파티션 키를 통해 관련 메시지를 같은 파티션으로 라우팅

### 6. 재처리 가능 (Replayability)

- 컨슈머 오프셋을 조정하여 과거 데이터 재처리 가능
- 새로운 분석이나 버그 수정 후 재처리 용이

## Kafka의 주요 구성 요소

### 1. 프로듀서 (Producer)

Kafka에 이벤트를 생성하여 발행하는 서비스이다.

```java

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements ExternalEventPublisher {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final EventTopicResolver eventTopicResolver;
	private final ObjectMapper mapper;

	@Override
	public void publish(Event event) {
		try {
			final String topic = eventTopicResolver.resolve(event);
			kafkaTemplate.send(topic, event.key(), mapper.writeValueAsString(event));
		} catch (JsonProcessingException e) {
			log.error("[KafkaEventPublisher] Event 객체를 JSON 문자열로 직렬화하는데 에러가 발생했습니다.", e);
			throw new CommerceException(e.getMessage());
		} catch (Exception e) {
			log.error("[KafkaEventPublisher] 이벤트를 발행하는데 예상하지 못한 에러가 발생했습니다.", e);
			throw new CommerceException(e.getMessage());
		}
	}
}
```

#### 주요 설정

- `acks`: 메시지 전송 신뢰도 설정 (0, 1, all)
- `retries`: 전송 실패 시 재시도 횟수
- `batch.size`: 배치 크기 설정

### 2. 컨슈머 (Consumer)

Kafka로부터 이벤트를 읽어서 처리하는 애플리케이션입니다.

```java

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedNotificationEventListener {
	private final SlackSendMessageClient slackSendMessageClient;
	private final ObjectMapper mapper;

	@KafkaListener(topics = "order.placed", groupId = "notification-consumer-group")
	public void handle(String message) {
		try {
			log.debug("[+OrderPlacedNotificationEventListener] 진입: Thread={}", Thread.currentThread().getName());
			OrderPlacedEvent event = mapper.readValue(message, OrderPlacedEvent.class);
			final String messageToSend = "[주문 확정🎉]"
				+ " orderId=" + event.orderId()
				+ " 주문 확정 일시=" + event.occurredAt();

			slackSendMessageClient.send(messageToSend);
		} catch (JsonProcessingException e) {
			log.error("[역직렬화 실패] Kafka 메시지를 OrderPlacedEvent로 변환하는데 실패했습니다. message={}",
				message, e);
		} catch (Exception e) {
			log.error("[알수 없는 에러 발생] 주문 확정 이후, 슬랙 메시지를 보내는데 에러가 발생했습니다.", e);
		}
	}

}
```

#### Consumer Group

- 메시지를 읽는 컨슈머들의 논리적 그룹
- 같은 `groupId`를 가진 컨슈머들이 하나의 그룹을 구성
- 파티션 할당 규칙: 같은 그룹 내에서 각 파티션은 오직 한 개의 컨슈머에게만 할당됨
    - 한 컨슈머가 여러 파티션을 담당할 수는 있음
    - 파티션 내 메시지 순서 보장 및 중복 처리 방지
- 서로 다른 그룹: 독립적으로 동작하므로 같은 파티션을 각자 읽을 수 있음

### 3. 토픽 (Topic)

이벤트가 저장되는 논리적인 카테고리 (데이터베이스의 테이블과 유사)

- ex) order.placed: 주문 및 결제 이벤트

### 4. 파티션 (Partition)

토픽을 구성하는 물리적 분할 단위이다.

```text
topic: order.placed (3개 파티션)

Partition 0: [msg1] [msg4] [msg7] ...
Partition 1: [msg2] [msg5] [msg8] ...
Partition 2: [msg3] [msg6] [msg9] ...
```

#### 파티션의 역할

- 병렬 처리: 여러 컨슈머가 동시에 처리 가능
- 확장성: 파티션 수만큼 처리량 증가
- 순서 보장: 같은 파티션 내에서만 순서 보장

### 5. 브로커 (Broker)

- Kafka 클러스터를 구성하는 물리적인 서버
- 프로듀서에게 메시지를 받아 이를 저장하고 컨슈머로 전송하는 역할을 한다.
- 카프카 클러스터 내에서 각 1개씩 존재하는 특별한 역할을 하는 브로커가 있다.
- 특별한 역할을 하는 브로커(Controller, Coordinator)들은, 일반적인 브로커의 역할도 같이 수행한다.

#### 1. Controller

- 브로커들의 관리자
- 브로커들의 상태를 모니터링
- Leader 파티션 재분배

```text
[정상 상황]
Broker1: Topic-A Partition-0 (Leader)
Broker2: Topic-A Partition-0 (Follower)
Broker3: Topic-A Partition-0 (Follower)

[Broker1 장애 발생!]
Controller가 감지
↓
Broker2: Topic-A Partition-0 (Leader로 승격!) ← Controller가 결정
Broker3: Topic-A Partition-0 (Follower)
```

#### 2.Coordinator

- 역할: 컨슈머 그룹의 관리자
- 구체적으로 하는 일
    - 컨슈머 그룹 모니터링
        - 각 컨슈머 그룹 내의 컨슈머들이 정상적으로 동작하는지 체크
        - 컨슈머가 주기적으로 heartbeat을 보내는데, 끊기면 장애로 판단
    - 리밸런싱(Rebalance) 수행
        - 컨슈머가 죽거나 새로 추가되면, 파티션 재분배

```text
[초기 상태]
Consumer1 → Partition 0, 1
Consumer2 → Partition 2, 3

[Consumer1 장애 발생!]
Coordinator가 감지
↓
Rebalance 시작
↓
Consumer2 → Partition 0, 1, 2, 3 (모두 담당)

[새로운 Consumer3 추가]
Rebalance 다시 발생
↓
Consumer2 → Partition 0, 1
Consumer3 → Partition 2, 3
```

### 6. 오프셋 (Offset)

- 파티션 내 메시지의 고유한 순차적 아이디

```
Partition 0
Offset: 0    1    2    3    4    5
       [A]  [B]  [C]  [D]  [E]  [F]
                       ↑
              Consumer's current offset
```

컨슈머는 자신이 읽은 마지막 오프셋을 기억하여 재시작 시 이어서 처리가 가능하다.

## Kafka 사용의 장단점

### 장점

#### 1. 시스템 간 느슨한 결합

- 프로듀서와 컨슈머가 서로를 알 필요가 없음
- 새로운 서비스 추가가 쉬움
- 서비스 변경이 다른 서비스에 영향 없음

#### 2. 높은 성능과 확장성

- 초당 수백만 건 처리 가능
- 수평 확장으로 성능 향상
- 배치 처리로 네트워크 효율성 극대화

#### 3. 데이터 영구성과 안정성

- 메시지를 디스크에 저장하여 손실 방지
- 복제를 통한 데이터 안정성
- 컨슈머 장애 시에도 데이터 유지 가능

#### 4. 재처리 가능

- 오프셋 조정으로 과거 데이터 재처리
- 새로운 분석이나 버그 수정 후 재실행 가능
- 감사(audit) 및 규정 준수에 유용

#### 5. 실시간 처리

- 낮은 지연시간으로 거의 실시간 처리
- 스트림 처리 API로 실시간 변환/집계

#### 6. 다양한 통합 옵션

- Kafka Connect로 데이터베이스, 파일 시스템 등과 쉽게 연동
- 풍부한 에코시스템과 라이브러리

### 단점

#### 1. 운영 복잡도 증가

- Zookeeper 또는 KRaft 모드 관리 필요
- 브로커, 파티션, 복제 설정 등 고려 사항 많음
- 모니터링과 튜닝에 전문 지식 필요

#### 2. 학습 곡선

- 파티션, 오프셋, 컨슈머 그룹 등 개발팀 전체의 개념 이해 필요
- 적절한 설정 값 찾기 어려움
- 디버깅 복잡

#### 3. 메시지 순서 보장의 제약

- 전체 토픽 레벨에서는 순서 보장 안됨
- 같은 파티션 내에서만 순서 보장
- 파티션 키 설계 중요

#### 4. 메시지 크기 제한

- 기본 메시지 크기 제한: 1MB
- 큰 파일이나 이미지 전송에는 부적합
- 큰 데이터는 별도 스토리지 사용 후 참조만 전달 권장

## 참고 자료

- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Confluent Kafka 튜토리얼](https://docs.confluent.io/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)

