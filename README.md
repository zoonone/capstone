# 🛠 Spring Boot JWT Authentication Server

## 📌 프로젝트 소개

Spring Boot 기반으로 구현한 **JWT 인증 서버**입니다.
회원가입, 로그인, JWT 토큰 발급 및 인증 기능을 제공합니다.

---

## 🚀 기술 스택

* **Backend**: Spring Boot 3
* **Language**: Java
* **Security**: Spring Security
* **Authentication**: JWT (Json Web Token)
* **Build Tool**: Gradle

---

## ✨ 주요 기능

### 1. 회원가입 (Signup)

* 이메일, 비밀번호, 이름으로 회원가입
* 비밀번호는 **BCrypt 암호화** 적용

```
POST /api/auth/signup
```

---

### 2. 로그인 (Login)

* 이메일 + 비밀번호 인증
* 성공 시 JWT 토큰 발급

```
POST /api/auth/login
```

**Response**

```json
{
  "accessToken": "JWT_TOKEN"
}
```

---

### 3. JWT 인증

* 요청 Header에 JWT 포함 시 인증 처리
* Stateless 방식 (세션 미사용)

```
Authorization: Bearer {JWT_TOKEN}
```

---

### 4. 인증 테스트 API

```
GET /api/test
```

* JWT가 유효하면 접근 가능

---

## 🔐 인증 흐름

```
회원가입 → 로그인 → JWT 발급 → API 요청 시 JWT 검증
```

---

## 🧱 프로젝트 구조

```
src/main/java/com/capstone/backend
 ┣ config
 ┃ ┗ SecurityConfig
 ┣ controller
 ┃ ┣ AuthController
 ┃ ┗ TestController
 ┣ security
 ┃ ┣ JwtProvider
 ┃ ┗ JwtAuthenticationFilter
 ┣ service
 ┃ ┗ AuthService
 ┣ repository
 ┗ entity
```

---

## ⚙️ Security 설정

* CSRF 비활성화
* 세션 사용 안함 (STATELESS)
* JWT 필터 적용
* `/api/auth/**` 경로는 인증 없이 접근 허용

---

## 📮 API 테스트 방법 (Postman)

1. 로그인 요청
2. JWT 토큰 복사
3. Authorization 설정

```
Type: Bearer Token
Token: 발급받은 JWT
```

---

## 📌 향후 계획

* [ ] 사용자 정보 조회 API (`/api/user/me`)
* [ ] OAuth 로그인 (카카오 / 구글)
* [ ] Refresh Token 구현
* [ ] 게시글 CRUD 기능 추가

---

## 💡 느낀 점

Spring Security와 JWT를 활용한 인증 구조를 직접 구현하면서
**Stateless 인증 방식과 필터 기반 인증 흐름**을 이해할 수 있었습니다.
