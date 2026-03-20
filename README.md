# Spring Boot Authentication Server

Spring Boot 3 기반 인증 서버입니다. 로컬 회원가입/로그인, JWT 발급, OAuth2 로그인 연동, 기본 보안 설정을 포함합니다.

## 주요 기능

- 로컬 회원가입 시 `BCryptPasswordEncoder`로 비밀번호 해시 저장
- 로컬 로그인 시 JWT access token 발급
- 로그인 실패 시 `"아이디 또는 비밀번호가 잘못되었습니다."`로 통일된 응답 반환
- Google, Kakao, Naver OAuth2 로그인 지원
- JWT 인증 필터를 통한 사용자 식별
- 입력값 검증과 예외별 HTTP 상태 코드 응답

## 기술 스택

- Java 17
- Spring Boot 3
- Spring Security
- Spring Security OAuth2 Client
- Spring Data JPA
- MySQL
- JJWT
- Gradle

## API

### `POST /api/auth/signup`

회원가입을 처리합니다.

Request

```json
{
  "email": "new@example.com",
  "password": "pw1234",
  "name": "New User"
}
```

Response

```text
회원가입 성공
```

### `POST /api/auth/login`

이메일과 비밀번호로 로그인하고 JWT를 발급합니다.

Request

```json
{
  "email": "user@example.com",
  "password": "pw1234"
}
```

Response

```json
{
  "accessToken": "JWT_TOKEN"
}
```

### `GET /api/auth/me`

현재 인증된 사용자의 이메일을 반환합니다. 인증 정보가 없으면 빈 문자열을 반환합니다.

### `GET /api/auth/oauth2/url/{provider}`

프론트엔드에서 사용할 OAuth2 시작 URL을 반환합니다.

예시:

```text
/oauth2/authorization/google
```

### `GET /`

서버 헬스체크용 기본 엔드포인트입니다.

## 인증 흐름

1. 사용자가 `/api/auth/signup`으로 가입합니다.
2. 서버는 비밀번호를 BCrypt로 암호화해 저장합니다.
3. 사용자가 `/api/auth/login`으로 로그인합니다.
4. 서버는 비밀번호를 `matches()`로 검증하고 JWT를 발급합니다.
5. 클라이언트는 `Authorization: Bearer {token}` 헤더로 요청합니다.
6. `JwtFilter`가 토큰을 검증하고 `SecurityContext`에 인증 정보를 저장합니다.

## 에러 응답 형식

모든 예외 응답은 아래 형식을 사용합니다.

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "아이디 또는 비밀번호가 잘못되었습니다."
}
```

대표 상태 코드:

- `400 Bad Request`: DTO 검증 실패
- `401 Unauthorized`: 로그인 실패, 인증 실패
- `409 Conflict`: 중복 이메일 회원가입
- `500 Internal Server Error`: 서버 내부 오류

## 환경 변수

`.env.example`을 참고해서 환경 변수를 설정할 수 있습니다.

필수 또는 주요 변수:

- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`
- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`
- `OAUTH2_REDIRECT_URI`

`JWT_SECRET`은 최소 32바이트 이상 길이의 랜덤 문자열을 사용해야 합니다.

예시:

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
```

## 실행

```bash
./gradlew bootRun
```

## 테스트

전체 테스트 실행:

```bash
./gradlew test
```

## 프로젝트 구조

```text
src/main/java/com/capstone/backend
├── controller
├── dto
├── entity
├── global
│   ├── exception
│   └── jwt
├── repository
├── security
│   └── oauth
└── service
```
