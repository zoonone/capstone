# Spring Boot Authentication Server

Spring Boot 3 기반 인증 서버입니다. 로컬 회원가입/로그인, JWT 발급, OAuth2 로그인, 전역 예외 처리, 입력값 검증을 포함합니다.

## 주요 기능

- 로컬 회원가입 시 `BCryptPasswordEncoder`로 비밀번호 해시 저장
- 로컬 로그인 성공 시 JWT access token 발급
- 로그인 실패와 회원가입의 이메일/비밀번호 관련 실패 시 `"이메일 혹은 비밀번호가 잘못되었습니다."`로 통일된 응답 반환
- Google, Kakao, Naver OAuth2 로그인 지원
- `JwtFilter`를 통한 Bearer 토큰 검증
- `@Valid`와 DTO 제약식을 통한 요청 검증
- `GlobalExceptionHandler`를 통한 공통 에러 응답 포맷 제공

## 기술 스택

- Java 21
- Spring Boot 3.5.11
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- Spring Security OAuth2 Client
- MySQL
- JJWT 0.11.5
- Gradle

## 실행 환경

기본 설정은 [application.yml](/Users/gimjun-won/Desktop/backend_git/src/main/resources/application.yml)을 따릅니다.

- 서버 포트: `8080`
- 기본 DB URL: `jdbc:mysql://localhost:3306/team_builder`
- JPA DDL 정책: `update`
- JWT 만료 시간 기본값: `3600000ms`
- 프론트 OAuth 콜백 기본값: `http://localhost:3000/oauth/callback`
- 기본 CORS 허용 Origin: `http://localhost:3000`, `http://127.0.0.1:3000`, `http://localhost:5173`, `http://127.0.0.1:5173`

## 환경 변수

주요 환경 변수는 아래와 같습니다.

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
- `CORS_ALLOWED_ORIGINS`
- `CORS_ALLOWED_METHODS`
- `CORS_ALLOWED_HEADERS`
- `CORS_EXPOSED_HEADERS`
- `CORS_ALLOW_CREDENTIALS`
- `CORS_MAX_AGE`

`JWT_SECRET`은 반드시 설정해야 하며 충분히 긴 랜덤 문자열을 사용하는 것이 안전합니다.

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

인증 서비스 테스트만 실행:

```bash
./gradlew test --tests com.capstone.backend.service.AuthServiceTest
```

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

Success Response

```text
회원가입 성공
```

중복 이메일 또는 이메일/비밀번호 관련 실패 응답 예시:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "이메일 혹은 비밀번호가 잘못되었습니다."
}
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

Success Response

```json
{
  "accessToken": "JWT_TOKEN"
}
```

실패 응답 예시:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "이메일 혹은 비밀번호가 잘못되었습니다."
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

서버 상태 확인용 엔드포인트입니다.

Response

```json
{
  "status": "ok",
  "message": "Backend server is running"
}
```

## 요청 검증

컨트롤러는 `@Valid`를 사용하고, DTO에서 아래 제약을 선언적으로 정의합니다.

- `LoginRequest`
- `email`: `@NotBlank`, `@Email`
- `password`: `@NotBlank`, `@Size(min = 4)`
- `SignupRequest`
- `email`: `@NotBlank`, `@Email`
- `password`: `@NotBlank`, `@Size(min = 4)`
- `name`: `@NotBlank`

인증 요청에서 `email` 또는 `password` 검증이 실패하면 응답 메시지는 `"이메일 혹은 비밀번호가 잘못되었습니다."`로 통일됩니다.

## 에러 응답 형식

모든 예외 응답은 아래 형식을 사용합니다.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "이메일 혹은 비밀번호가 잘못되었습니다."
}
```

대표 상태 코드는 다음과 같습니다.

- `400 Bad Request`: DTO 검증 실패
- `401 Unauthorized`: 로그인 실패 또는 인증 실패
- `409 Conflict`: 회원가입 충돌 처리
- `500 Internal Server Error`: 서버 내부 오류

## 프로젝트 구조

```text
src/main/java/com/capstone/backend
├── BackendApplication.java
├── controller
│   ├── AuthController.java
│   └── HomeController.java
├── dto
│   ├── LoginRequest.java
│   ├── SignupRequest.java
│   └── TokenResponse.java
├── entity
│   ├── AuthProvider.java
│   └── User.java
├── global
│   ├── exception
│   │   ├── BusinessException.java
│   │   ├── ConflictException.java
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── UnauthorizedException.java
│   └── jwt
│       ├── JwtFilter.java
│       └── JwtUtil.java
├── repository
│   └── UserRepository.java
├── security
│   ├── SecurityConfig.java
│   └── oauth
│       ├── CustomOAuth2User.java
│       ├── CustomOAuth2UserService.java
│       ├── GoogleOAuth2UserInfo.java
│       ├── KakaoOAuth2UserInfo.java
│       ├── NaverOAuth2UserInfo.java
│       ├── OAuth2AuthenticationSuccessHandler.java
│       ├── OAuth2UserInfo.java
│       └── OAuth2UserInfoFactory.java
└── service
    └── AuthService.java
```
