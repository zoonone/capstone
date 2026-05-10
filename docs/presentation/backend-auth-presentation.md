# 인증 서버 발표 자료

## 슬라이드 1. 제목
- 제목: Spring Boot 인증 서버 발표
- 설명:
  - JWT + OAuth2 기반 인증 백엔드
  - 구조 설명과 실제 동작 코드 중심 발표
- 발표 멘트:
이번 발표에서는 단순 기능 소개가 아니라, 이 프로젝트가 어떤 구조로 이루어져 있고 실제 코드가 어떻게 동작하는지를 함께 설명하겠습니다.

## 슬라이드 2. 프로젝트 개요
- 핵심 기능:
  - 로컬 회원가입 / 로그인
  - JWT 발급
  - Google, Kakao, Naver OAuth2 로그인
  - 전역 예외 처리와 입력값 검증
  - 테스트 코드 작성
- 발표 멘트:
실제 서비스에서 자주 필요한 인증 기능들을 하나의 서버 구조 안에서 통합했습니다.

## 슬라이드 3. 패키지 구조
- 이미지: `assets/package-structure.svg`
- 핵심 설명:
  - `controller`: HTTP 요청과 응답
  - `service`: 핵심 비즈니스 로직
  - `repository`: DB 접근
  - `security`, `global`: 인증/예외 처리
- 발표 멘트:
구조를 이렇게 나눈 이유는 변경 지점을 분리하고, 테스트하기 쉽게 만들기 위해서입니다.

## 슬라이드 4. 시스템 구조
- 이미지: `assets/system-architecture.svg`
- 핵심 설명:
  - 요청은 컨트롤러에서 시작
  - 서비스가 회원가입/로그인을 처리
  - 저장과 조회는 repository에서 수행
  - SecurityConfig, JwtFilter, JwtUtil이 인증 상태를 관리
- 발표 멘트:
요청은 컨트롤러로 들어오지만, 인증 여부 판단은 Spring Security 체인 안에서 처리됩니다.

## 슬라이드 5. 실제 코드: AuthController
- 이미지: `assets/auth-controller-code.svg`
- 핵심 설명:
  - `@Valid`와 `@RequestBody`로 요청 검증
  - 컨트롤러는 서비스 호출과 응답 포맷에 집중
- 발표 멘트:
컨트롤러를 얇게 유지해서 HTTP 처리와 비즈니스 로직을 분리했습니다.

## 슬라이드 6. 실제 코드: AuthService
- 이미지: `assets/auth-service-code.svg`
- 핵심 설명:
  - 이메일 중복 검사
  - 비밀번호 해시 저장
  - provider 검사
  - 로그인 성공 시 JWT 발급
- 발표 멘트:
핵심 기능은 서비스에 모아두었고, 이 계층이 실제 로그인과 회원가입 동작을 담당합니다.

## 슬라이드 7. 로컬 로그인 흐름
- 이미지: `assets/local-login-flow.svg`
- 발표 멘트:
로그인 요청이 들어오면 이메일로 사용자를 찾고, 비밀번호를 BCrypt로 비교한 뒤, 성공하면 JWT를 반환합니다.

## 슬라이드 8. 실제 코드: JwtFilter
- 이미지: `assets/jwt-filter-code.svg`
- 핵심 설명:
  - Authorization 헤더에서 Bearer 토큰 추출
  - 토큰 검증 후 SecurityContext에 인증 객체 저장
- 발표 멘트:
이 필터 덕분에 매 요청마다 서버가 로그인 상태를 직접 판별할 수 있습니다.

## 슬라이드 9. OAuth2 로그인 흐름
- 이미지: `assets/oauth2-flow.svg`
- 핵심 설명:
  - 소셜 로그인 시작
  - provider별 사용자 정보 수신
  - 회원 upsert
  - JWT 발급 후 프론트로 리다이렉트
- 발표 멘트:
로컬 로그인과 소셜 로그인 모두 최종적으로는 같은 인증 구조 안으로 들어오게 설계했습니다.

## 슬라이드 10. 실제 코드: OAuth2 사용자 처리
- 이미지: `assets/oauth2-code.svg`
- 핵심 설명:
  - `OAuth2UserInfoFactory`가 provider별 클래스 선택
  - Google, Kakao, Naver 응답 구조 차이를 추상화
- 발표 멘트:
이 구조를 둔 이유는 소셜 로그인 제공자가 늘어나도 확장 가능하게 만들기 위해서입니다.

## 슬라이드 11. 실제 코드: 예외 처리와 검증
- 이미지: `assets/exception-code.svg`
- 핵심 설명:
  - DTO 검증
  - 인증 관련 실패 메시지 통일
  - `GlobalExceptionHandler`로 공통 응답 형식 유지
- 발표 멘트:
보안상 어떤 값이 틀렸는지를 자세히 알려주지 않기 위해 메시지를 통일했습니다.

## 슬라이드 12. 예외 처리와 테스트 전략
- 이미지: `assets/exception-test-overview.svg`
- 테스트 대상:
  - `AuthServiceTest`
  - `AuthControllerApiTest`
  - `JwtUtilTest`
  - `JwtFilterTest`
  - `CustomOAuth2UserServiceTest`
- 발표 멘트:
정상 동작뿐 아니라 실패 상황까지 검증해서 API 동작을 안정적으로 유지했습니다.

## 슬라이드 13. 실제 코드: 테스트
- 이미지: `assets/test-code.svg`
- 핵심 설명:
  - 서비스 테스트는 JWT 생성과 로직 검증
  - 컨트롤러 테스트는 상태 코드와 메시지 검증
- 발표 멘트:
이 프로젝트는 기능 구현에 그치지 않고, 테스트로 결과를 확인하는 구조까지 만들었습니다.

## 슬라이드 14. 시연 및 마무리
- 시연 순서:
  - 회원가입
  - 로그인 후 JWT 확인
  - `/api/auth/me` 호출
  - OAuth2 URL 설명
- 한 줄 정리:
  - 로컬 로그인과 소셜 로그인을 하나의 보안 구조 안에서 통합한 인증 서버
- 발표 멘트:
정리하면 이 프로젝트의 강점은 인증 기능을 구현한 것뿐 아니라, 구조와 보안 이유를 설명할 수 있게 만든 점입니다.
