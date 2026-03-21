# ChalkChalk 서버 실시간 음성 채팅 분석 및 iOS 구현 보고서

작성일: 2026-02-27  
대상 저장소: `chalkchalk-server`

## 1. 핵심 결론 요약

현재 서버의 실시간 음성 채팅은 **WebRTC 미디어 서버(SFU/MCU)가 아니라 WebRTC 시그널링 중계 서버**로 구현되어 있습니다.

- 미디어(오디오 RTP)는 클라이언트(iOS ↔ iOS) 간 P2P WebRTC로 직접 송수신
- 서버는 STOMP over WebSocket + Redis Pub/Sub로 Offer/Answer/ICE/JOIN/LEAVE 시그널만 중계
- 방 참여 상태(`ParticipantState.JOINED`)가 아니면 음성 채널 구독/전송이 차단됨
- iOS 구현에서 가장 중요한 선행 순서는 `REST join → WS 연결(token query) → STOMP subscribe/send`

---

## 2. 조사 근거(코드 레퍼런스)

아래 파일들을 기준으로 분석했습니다.

- WebSocket/STOMP 설정
  - `src/main/java/com/writingboard/server/global/config/WebSocketConfig.java`
  - `src/main/java/com/writingboard/server/global/websocket/JwtHandshakeInterceptor.java`
  - `src/main/java/com/writingboard/server/global/websocket/StompChannelInterceptor.java`
  - `src/main/java/com/writingboard/server/global/websocket/StompPrincipal.java`
- 음성 시그널링
  - `src/main/java/com/writingboard/server/domain/voice/controller/VoiceSignalingController.java`
  - `src/main/java/com/writingboard/server/domain/voice/service/VoiceService.java`
  - `src/main/java/com/writingboard/server/domain/voice/service/VoiceRedisPublisher.java`
  - `src/main/java/com/writingboard/server/domain/voice/service/VoiceRedisSubscriber.java`
  - `src/main/java/com/writingboard/server/domain/voice/dto/request/VoiceSignalRequest.java`
  - `src/main/java/com/writingboard/server/domain/voice/dto/response/VoiceSignalDto.java`
  - `src/main/java/com/writingboard/server/domain/voice/enums/SignalType.java`
  - `src/main/java/com/writingboard/server/domain/voice/exception/VoiceErrorCode.java`
- 회의실 참여 상태(음성 권한 선행조건)
  - `src/main/java/com/writingboard/server/domain/meeting/controller/RoomController.java`
  - `src/main/java/com/writingboard/server/domain/meeting/service/RoomService.java`
  - `src/main/java/com/writingboard/server/domain/meeting/entity/RoomParticipant.java`
  - `src/main/java/com/writingboard/server/domain/meeting/dto/response/RoomDetailResponse.java`
- 인증/보안
  - `src/main/java/com/writingboard/server/global/config/SecurityConfig.java`
  - `src/main/java/com/writingboard/server/domain/auth/controller/AuthController.java`
  - `src/main/java/com/writingboard/server/domain/auth/service/AuthService.java`
  - `src/main/java/com/writingboard/server/domain/auth/jwt/JwtProvider.java`
- Redis Pub/Sub
  - `src/main/java/com/writingboard/server/global/config/RedisPubSubConfig.java`
- 문서
  - `README.md` (WebSocket 엔드포인트/아키텍처 섹션)

---

## 3. 현재 서버의 실시간 음성 채팅 구현 방식 (정밀 분석)

## 3.1 전송 계층과 브로커 구조

서버는 STOMP 메시지 브로커를 다음처럼 구성합니다.

- Broker: `/topic`, `/queue`
- App prefix: `/app`
- User prefix: `/user`
- Endpoint: `/ws-stomp`

핵심 의미:

- iOS는 `/app/room/{roomUuid}/voice`로 SEND
- iOS는 `/topic/room/{roomUuid}/voice`를 SUBSCRIBE
- 에러 수신은 `/user/queue/errors` SUBSCRIBE

또한 `RedisMessageListenerContainer`가 `voice:room:*` 패턴을 구독해, 멀티 인스턴스에서도 음성 시그널을 fanout 하도록 되어 있습니다.

## 3.2 인증/인가 흐름 (중요)

인증은 2단계입니다.

1. WebSocket Handshake 단계
- `ws://host/ws-stomp?token={JWT}` 형태의 query token 필수
- `JwtHandshakeInterceptor`가 query string의 `token`을 검증
- 유효하면 `memberId`를 session attribute에 저장, 아니면 handshake 거절

2. STOMP CONNECT/SUBSCRIBE 단계
- `CONNECT` 시 `Authorization: Bearer {JWT}` 헤더가 있으면 재검증 후 Principal 설정
- 없으면 handshake에서 저장된 `memberId`로 Principal 설정
- `SUBSCRIBE /topic/room/{roomUuid}...` 시 회의실 참여자(`JOINED`)인지 검증

실무적으로 iOS에서 반드시 지켜야 하는 점:

- **Handshake query token이 사실상 필수**입니다. CONNECT 헤더만으로는 handshake를 통과할 수 없습니다.
- 음성 채널 구독 전에 `joinRoom` REST를 성공시켜 `ParticipantState=JOINED` 상태를 만들어야 합니다.

## 3.3 음성 시그널링 메시지 파이프라인

엔드투엔드 경로:

1. iOS SEND  
`/app/room/{roomUuid}/voice` + payload(`VoiceSignalRequest`)

2. Controller
- `@MessageMapping("/room/{roomUuid}/voice")`
- `Principal`에서 `memberId` 추출 후 `VoiceService.sendSignal(...)` 호출

3. Service validation
- room 존재 확인
- room 상태가 `OPEN`인지 확인
- participant가 `JOINED`인지 확인
- data null/blank/길이>10000 검사

4. Redis publish
- 채널: `voice:room:{roomUuid}`
- payload: `VoiceSignalDto`

5. Redis subscribe → STOMP broadcast
- Redis subscriber가 수신 후 `/topic/room/{roomUuid}/voice`로 `convertAndSend`

중요 특성:

- 서버는 대상 피어 라우팅을 하지 않습니다.
- 방 구독자 전체에게 브로드캐스트합니다(발신자 포함).
- 클라이언트가 `senderId`/`data`를 보고 수신 필터링해야 합니다.

## 3.4 시그널 데이터 계약(Contract)

### Client → Server (`VoiceSignalRequest`)

```json
{
  "signalType": "OFFER | ANSWER | ICE_CANDIDATE | JOIN | LEAVE",
  "data": "string(최대 10000자)"
}
```

- `signalType`: 필수 enum
- `data`: 필수 문자열, 공백 불가, max 10000
- `data`는 JSON object가 아니라 **문자열 필드**임

### Server → Client (`VoiceSignalDto`)

```json
{
  "roomUuid": "string",
  "senderId": 123,
  "senderName": "홍길동",
  "signalType": "OFFER | ANSWER | ICE_CANDIDATE | JOIN | LEAVE",
  "data": "string",
  "timestamp": "2026-02-27T00:00:00Z"
}
```

## 3.5 회의실 참여 상태와 음성 권한

음성 사용 전제:

- `POST /api/rooms/{roomUuid}/join` 성공 필요
- 그렇지 않으면 SUBSCRIBE/SEND 과정에서 `NOT_PARTICIPANT` 계열 오류 가능

퇴장 처리:

- `POST /api/rooms/{roomUuid}/leave`를 명시적으로 호출해야 `LEFT` 전환
- WebSocket disconnect 이벤트로 자동 leave 처리하는 로직은 현재 없음

즉, iOS가 비정상 종료/네트워크 단절 시 leave를 못 보내면 서버의 참여 상태가 남을 수 있습니다.

## 3.6 에러 처리 방식

음성 도메인 에러 코드:

- `VOICE_001` INVALID_SIGNAL
- `VOICE_002` SIGNAL_DATA_TOO_LARGE
- `VOICE_003` ROOM_NOT_FOUND
- `VOICE_004` ROOM_CLOSED
- `VOICE_005` NOT_PARTICIPANT
- `VOICE_006` MEMBER_NOT_FOUND

WebSocket 처리 중 컨트롤러 예외는 `/user/queue/errors`로 문자열 메시지를 전송합니다.

주의점:

- Interceptor 단계 예외(`SUBSCRIBE` 차단 등)는 컨트롤러 레벨 예외 핸들러가 아닌 STOMP ERROR frame/연결 종료로 나타날 수 있으므로 iOS에서 frame-level 에러 처리도 필요합니다.

## 3.7 확장성/신뢰성 관점에서 관찰된 점

1. 멀티 인스턴스 확장성
- Redis Pub/Sub로 인스턴스 간 신호 전달이 가능함

2. 전달 보장
- Redis Pub/Sub 특성상 durable queue가 아니므로 일시 단절 시 메시지 유실 가능

3. 실패 전파
- `VoiceRedisPublisher.publish()`는 예외를 catch 후 로그만 남김(호출자에게 실패 미전파)
- 즉, 발신 클라이언트는 서버 publish 실패를 즉시 알기 어려움

4. 토큰 만료 표기 불일치
- JWT 실제 access 만료 설정: 2시간(`application-*.yml`)
- `TokenResponse.expiresIn`: 1800초(30분)로 고정 반환
- iOS refresh 스케줄링 시 혼선 가능

---

## 4. iOS 실시간 음성 채팅 구현 가이드 (현 서버 방식 100% 호환 기준)

## 4.1 권장 iOS 아키텍처

- `AuthService`
  - guest/social login
  - refresh token
- `RoomService`
  - room join/leave/getRoomDetail
- `StompTransport`
  - WebSocket 연결, STOMP frame 송수신, reconnect
- `VoiceSignalingService`
  - `/app/room/{uuid}/voice` 송신
  - `/topic/room/{uuid}/voice` 수신
  - `/user/queue/errors` 수신
- `WebRTCSessionManager`
  - peer별 `RTCPeerConnection` 관리
  - offer/answer/ice 처리
- `AudioSessionManager`
  - `AVAudioSession` 설정, 라우트/인터럽션 대응

## 4.2 iOS 구현 순서 (필수)

1. 로그인
- `POST /api/auth/guest-login` 또는 `POST /api/auth/social-login`
- `accessToken`, `refreshToken` 확보

2. 회의실 입장
- `POST /api/rooms/{roomUuid}/join`
- 서버 participant state를 `JOINED`로 만든 뒤 진행

3. 참가자 목록 동기화
- `GET /api/rooms/{roomUuid}`로 현재 active participant 목록 확인

4. WebSocket 연결
- URL: `wss://{host}/ws-stomp?token={accessToken}`
- 운영에서는 `wss` 필수

5. STOMP CONNECT
- Native header: `Authorization: Bearer {accessToken}` 권장
- heartbeat 설정(예: `10000,10000`) 권장

6. STOMP SUBSCRIBE
- `/topic/room/{roomUuid}/voice`
- `/user/queue/errors`

7. JOIN 시그널 송신
- `signalType=JOIN`
- `data`에 본인 식별정보/세션정보(JSON 문자열) 포함 권장

8. WebRTC peer 연결 시작
- 기존 참가자들에게 offer 생성/전송
- 이후 answer/ice 교환

9. 종료 처리
- `signalType=LEAVE` 송신
- `POST /api/rooms/{roomUuid}/leave` 호출
- STOMP disconnect + WebSocket close

## 4.3 `data` 필드 표준(클라이언트 내부 규약) 제안

서버 DTO에 `targetMemberId`가 없으므로, iOS-Android/iOS-iOS 간 상호운용을 위해 `data` 안쪽 JSON 규약을 통일해야 합니다.

권장 포맷 예시:

```json
{
  "v": 1,
  "from": 101,
  "to": 202,
  "roomUuid": "xxxx",
  "sdp": "...",
  "candidate": "...",
  "sdpMid": "0",
  "sdpMLineIndex": 0,
  "reason": "user_leave"
}
```

`VoiceSignalRequest`에는 이 JSON을 문자열로 넣습니다.

```json
{
  "signalType": "ICE_CANDIDATE",
  "data": "{\"v\":1,\"from\":101,\"to\":202,\"candidate\":\"...\",\"sdpMid\":\"0\",\"sdpMLineIndex\":0}"
}
```

클라이언트 수신 처리 규칙:

- `senderId == myMemberId`인 메시지는 기본 무시(자기 echo)
- `data.to`가 내 memberId가 아니면 무시(브로드캐스트 방식을 클라이언트 필터링으로 보완)

## 4.4 Swift 구현 예시 (핵심 로직)

```swift
struct VoiceSignalRequest: Encodable {
    let signalType: String
    let data: String
}

struct SignalEnvelope: Codable {
    let v: Int
    let from: Int64
    let to: Int64?
    let roomUuid: String
    let sdp: String?
    let candidate: String?
    let sdpMid: String?
    let sdpMLineIndex: Int32?
    let reason: String?
}

func makeSignalData(_ envelope: SignalEnvelope) throws -> String {
    let raw = try JSONEncoder().encode(envelope)
    guard let text = String(data: raw, encoding: .utf8) else {
        throw NSError(domain: "voice", code: -1)
    }
    return text
}

func sendOffer(to peerId: Int64, sdp: String) throws {
    let envelope = SignalEnvelope(
        v: 1, from: myMemberId, to: peerId, roomUuid: roomUuid,
        sdp: sdp, candidate: nil, sdpMid: nil, sdpMLineIndex: nil, reason: nil
    )
    let req = VoiceSignalRequest(signalType: "OFFER", data: try makeSignalData(envelope))
    stomp.send(destination: "/app/room/\(roomUuid)/voice", body: req)
}
```

## 4.5 WebRTC(iOS) 구현 체크포인트

1. 라이브러리
- `GoogleWebRTC`(공식 WebRTC iOS 빌드) 사용 권장

2. PeerConnection 설계
- 다자 통화 시 `peerMemberId -> RTCPeerConnection` 맵 관리
- `RTCPeerConnectionFactory`는 앱 생명주기에서 재사용

3. ICE 서버
- 서버 코드에는 STUN/TURN 제공 로직이 없음
- iOS 앱 설정/원격 config로 TURN 포함 ICE 서버 목록 주입 필요

4. 오디오 세션
- `AVAudioSession` category `.playAndRecord`
- mode `.voiceChat`
- options `.allowBluetooth`, `.defaultToSpeaker` 등 UX 요구에 맞춰 적용

5. 인터럽션/백그라운드
- 통화 중 전화 수신/오디오 라우트 변경 처리
- 앱 백그라운드 전환 시 정책(통화 유지/종료) 명확화

## 4.6 STOMP 연결 안정화 전략

필수 권장:

- 지수 백오프 재연결(예: 1s, 2s, 4s, ... 최대 30s)
- 재연결 성공 시 `SUBSCRIBE` 재등록 + `JOIN` 재송신 + peer 재협상
- heartbeat timeout 감지 후 소켓 재생성
- 토큰 만료 시 refresh 후 WebSocket URL(query token) 재생성

토큰 관련 주의:

- 서버 응답 `expiresIn=1800`과 실제 access token 만료 설정(2시간)이 불일치
- iOS는 우선 `expiresIn` 기반 선제 refresh(보수적)로 운영하는 것이 안전

## 4.7 서버 제약사항을 반영한 iOS 예외 처리 매트릭스

1. `VOICE_005` / SUBSCRIBE 거절
- 원인: join 미실행 또는 LEFT 상태
- 처리: `POST /join` 재시도 후 WS 재연결/재구독

2. `VOICE_004`
- 원인: room closed
- 처리: 통화 UI 종료, room list 화면 복귀

3. WebSocket handshake 실패
- 원인: query token 누락/만료/유효하지 않음
- 처리: refresh token 발급 후 URL 재생성

4. 수신 메시지 파싱 실패
- 원인: `data` 문자열 규약 불일치
- 처리: 버전(`v`) 기반 파서 분기, 호환되지 않으면 무시 + 로깅

---

## 5. iOS 구현 체크리스트 (실행용)

- [ ] 로그인 후 access/refresh token 저장(Keychain)
- [ ] 음성 시작 전 `POST /api/rooms/{uuid}/join` 호출
- [ ] `wss://host/ws-stomp?token=...` 형태로 연결
- [ ] STOMP CONNECT에 `Authorization: Bearer ...` 헤더 포함
- [ ] `/topic/room/{uuid}/voice`, `/user/queue/errors` 구독
- [ ] `VoiceSignalRequest`(`signalType`, `data`) 포맷 준수
- [ ] `data` 내부 JSON 규약(versioned) 정의 및 공통 파서 적용
- [ ] 브로드캐스트 수신 시 `to`/`senderId` 필터링
- [ ] LEAVE 시그널 + `POST /leave` + disconnect 순서 보장
- [ ] 재연결 시 subscribe 재등록 및 peer 재협상
- [ ] AVAudioSession 인터럽션/라우트 변경 처리
- [ ] TURN 서버 포함 ICE 구성

---

## 6. 서버 개선 제안 (iOS 구현 난이도/안정성 관점)

현재 방식으로도 iOS 구현은 가능하지만, 아래 개선을 하면 장애율과 클라이언트 복잡도가 크게 줄어듭니다.

1. 시그널 DTO에 `targetMemberId` 추가
- 현재는 `data` 문자열 파싱으로 우회 중

2. WebSocket disconnect 이벤트에서 participant 자동 `LEFT` 처리
- 앱 강제종료 시 stale participant 감소

3. `VoiceRedisPublisher` 실패를 호출자에게 전파
- 발신자에게 즉시 오류 반환 가능

4. `TokenResponse.expiresIn`과 실제 JWT 만료 시간 일치
- 클라이언트 refresh 로직 단순화

5. `/topic/room/{uuid}/voice` 외 `user queue` 기반 직접 라우팅 옵션 제공
- 다자 통화에서 브로드캐스트 오버헤드 감소

---

## 7. 최종 정리

현재 서버는 WebRTC 음성의 **시그널링 중계**를 STOMP + Redis로 안정적으로 처리하도록 구성되어 있으며, iOS에서 구현할 때 가장 중요한 포인트는 다음 3가지입니다.

1. **반드시 먼저 회의실 JOIN 상태를 만든다** (`POST /join`)  
2. **WebSocket handshake query token을 반드시 포함한다** (`/ws-stomp?token=...`)  
3. **브로드캐스트 시그널링 모델을 클라이언트 필터링/재협상 전략으로 보완한다**

이 3가지를 지키면 현재 서버 구조를 변경하지 않고도 iOS 실시간 음성 채팅을 구현할 수 있습니다.
