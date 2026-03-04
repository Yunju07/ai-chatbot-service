# API 명세

## 공통
- Base URL: `http://localhost:8080`
- 인증: `Authorization: Bearer <JWT>`
- 응답 시간은 구현/환경에 따라 달라질 수 있습니다.

## 인증

### 회원가입
- Method: `POST`
- Path: `/auth/signup`
- Auth: 불필요
- Request (JSON)
  - `email` (string, required)
  - `password` (string, required, 8~72)
  - `name` (string, required)
- Response 200 (JSON)
  - `token` (string)
  - `tokenType` (string, default: `Bearer`)
  - `expiresInMinutes` (number)

### 로그인
- Method: `POST`
- Path: `/auth/login`
- Auth: 불필요
- Request (JSON)
  - `email` (string, required)
  - `password` (string, required)
- Response 200 (JSON)
  - `token` (string)
  - `tokenType` (string, default: `Bearer`)
  - `expiresInMinutes` (number)

## 헬스 체크

### 서버 상태 확인
- Method: `GET`
- Path: `/health`
- Auth: 불필요
- Response 200 (JSON)
  - `status` (string, `ok`)

## 대화

### 대화 생성
- Method: `POST`
- Path: `/chats`
- Auth: 필요
- Request (JSON)
  - `question` (string, required)
  - `isStreaming` (boolean, optional, default: false)
  - `model` (string, optional)
- Response 200 (JSON)
  - `threadId` (string)
  - `chatId` (string)
  - `question` (string)
  - `answer` (string)
  - `createdAt` (string, ISO-8601)
- Streaming Response (SSE)
  - Content-Type: `text/event-stream`
  - `event: chunk` → `data: <string>`
  - `event: done` → `data: {ChatCreateResponse}`

### 대화 목록 조회 (스레드 그룹)
- Method: `GET`
- Path: `/chats`
- Auth: 필요
- Query
  - `page` (number, default: 0)
  - `size` (number, default: 20)
  - `sort` (string, `asc|desc`, default: `desc`)
- Response 200 (JSON)
  - `items` (array)
    - `id` (string, threadId)
    - `createdAt` (string, ISO-8601)
    - `chats` (array)
      - `id` (string)
      - `question` (string)
      - `answer` (string)
      - `createdAt` (string)
  - `page` (number)
  - `size` (number)
  - `totalItems` (number)

### 스레드 삭제
- Method: `DELETE`
- Path: `/chats/threads/{threadId}`
- Auth: 필요
- Response 200 (no body)

## 에러 응답
- Response (JSON)
  - `message` (string)
