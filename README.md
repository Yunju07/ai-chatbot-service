# AI Chatbot Service

## 개요
- VIP onboarding 시연을 목표로 한 API 서버입니다.
- 목표는 3시간 내에 "AI를 API로 활용 가능함"을 보여주면서, 이후 확장 가능한 구조를 확보하는 것이었습니다.
- 현재 구현은 인증/JWT, 대화/스레드, 피드백, 관리자 리포트(CSV)까지 포함합니다.

## 기술 스택
- Kotlin `1.9.24`
- Spring Boot `3.3.2`
- Java `17`
- Spring Data JPA, Spring Security, JWT
- DB: PostgreSQL `15.15`

## 실행 방법

### 1) 사전 준비
- Java 17+
- PostgreSQL 실행
- DB 생성: `ai_chatbot`

### 2) 환경 변수(선택)
- `DB_URL` (default: `jdbc:postgresql://localhost:5432/ai_chatbot`)
- `DB_USERNAME` (default: `${USER}`)
- `DB_PASSWORD` (default: empty)
- `OPENAI_ENABLED` (default: `false`, `true`면 OpenAI 연동 사용)
- `OPENAI_API_KEY` (`OPENAI_ENABLED=true`일 때 필수)
- `OPENAI_BASE_URL` (default: `https://api.openai.com`)

`OPENAI_ENABLED=true`여도 API 호출 실패/키 누락 시에는 자동으로 mock 응답으로 폴백됩니다.

관리자 시연이 필요하면 bootstrap 계정을 사용할 수 있습니다.
- `APP_BOOTSTRAP_ADMIN_ENABLED=true`
- `APP_BOOTSTRAP_ADMIN_EMAIL=admin@example.com`
- `APP_BOOTSTRAP_ADMIN_PASSWORD=admin1234`
- `APP_BOOTSTRAP_ADMIN_NAME=Administrator` (선택)

### 3) 실행
```bash
./gradlew bootRun
```

### 4) API 확인
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- API 문서: [`docs/API.md`](docs/API.md)

## 설계

### 1) 상황 분석
- 3시간 내 시연 필요
- repo는 지속 확장 가능해야 함 
- 고객은 API Spec 이해가 깊지 않음 → 이해하기 쉬운 요청/응답 구조 제공 필요
- 향후 대외비 문서 학습(RAG/모델 교체) 가능성 높음
- 요구사항 조율이 불가한 상황

완성형 제품보다는 "확장 가능한 구조 + 최소한의 AI 연동 시연"을 핵심 목표로 설정했습니다.

### 2) 기능 우선순위
시연에 필요한 **최소한의 기능(MVP)**와 부수적인 기능으로 분리하여 우선순위를 설정하였습니다.

1. (MVP) 회원가입 / 로그인 (JWT 인증 포함)
2. (MVP) 대화 생성 및 스레드 관리
3. 피드백 생성 및 조회
4. 사용자 활동 집계 및 보고서 생성
5. 실제 인공지능 호출 연동

AI 호출 자체를 먼저 붙일 수도 있었지만, 멀티 사용자 시나리오를 고려해 인증/권한을 먼저 고정했습니다.

### 2-1) AI 연동을 마지막에 적용한 이유
- 초기 단계에서는 외부 API 영향 없이 핵심 도메인(인증, 권한, 소유권, 스레드 규칙)을 먼저 안정화했습니다.
- API 요구사항을 mock 기반으로 먼저 고정해, 테스트를 진행할 수 있게 했습니다.
- 마지막 단계에서 `AiClient` 구현체만 OpenAI로 교체해 기존 기능 영향 범위를 최소화했습니다.
- 실연동 이후에도 운영 안정성을 위해 실패 시 **mock 응답으로 폴백**하는 안전장치를 유지했습니다.

### 3) 설계 전략

#### 사용자 관리 및 인증
- 이메일 unique + 회원가입 중복 체크
- JWT에 `userId`, `role` 포함
- 비밀번호는 BCrypt 해시 저장
- 권한은 enum(`MEMBER`, `ADMIN`) 관리
- Security + JWT 필터 기반 인증

#### 대화(chat) 관리
- 스레드 규칙: 마지막 질문 후 30분 이내면 기존 스레드 유지, 초과 시 신규 스레드 생성
- AI 호출 인터페이스(`AiClient`) 분리로 provider 교체 가능
- 목록 조회는 권한 기반 접근 + 정렬/페이지네이션 지원
- 삭제는 soft delete(`deletedAt`)
- 스트리밍은 SSE(`chunk`, `done`)

#### 피드백 관리
- `(userId, chatId)` 중복 방지(서비스 레벨 체크)
- 일반 사용자는 본인 채팅에만 생성 가능, 관리자는 전체 가능
- 상태는 enum(`PENDING`, `RESOLVED`)이며 관리자만 변경 가능
- 조회는 정렬/페이지네이션/긍정-부정 필터 지원

#### 분석 및 보고
- 관리자 전용 API
- 회원가입/로그인/대화생성 이벤트를 ActivityLog에 기록 후 집계
- 최근 24시간 기준 집계
- CSV 보고서 제공(대화 + 사용자 이메일 포함)

## AI 활용
- 과제 분석/문서 초안: ChatGPT 활용
- MVP 구현/리팩터링: Codex 활용
- 검증 방식: AI 제안 결과를 그대로 반영하지 않고, 요구사항(권한/소유권/스레드 규칙/응답 포맷) 기준으로 수동 검증 후 반영

## 트러블슈팅

### SSE 실연동 + 폴백 처리
이번 구현에서 가장 어려웠던 부분은 SSE 실연동이었습니다.

- `/chats`에서 OpenAI 스트리밍 응답을 받아 `chunk`로 내려주는 과정에서 응답이 중간에 끊기는 문제가 반복됐습니다.
- 특히 끊김이 발생했을 때, 우리 코드 문제인지 외부 API/네트워크 문제인지 원인을 명확히 분리하기가 어려웠습니다.
- 실행 환경 제약도 있어 end-to-end 연동 테스트를 충분히 반복하지 못했습니다.

그래서 현재는 실연동이 실패하면 mock으로 자동 폴백되도록 안전장치를 먼저 적용했습니다.  
다음 단계에서는 실제 키/네트워크 환경에서 스트리밍 테스트를 반복해 원인을 좁히고 안정화할 계획입니다.
