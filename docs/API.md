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

## 에러 응답
- Response (JSON)
  - `message` (string)

