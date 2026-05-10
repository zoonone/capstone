package com.capstone.backend;

import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.CorsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "jwt.secret=4f9a2c7e1b6d8a0c3e5f7b9d2a4c6e8f")
class BackendApplicationTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private CorsProperties corsProperties;

    @Test
    @DisplayName("애플리케이션 핵심 보안 빈이 정상적으로 로드된다")
    void contextLoadsCoreSecurityBeans() {
        assertNotNull(jwtUtil);
        assertNotNull(jwtFilter);
        assertNotNull(passwordEncoder);
        assertNotNull(securityFilterChain);
        assertNotNull(corsConfigurationSource);
        assertNotNull(corsProperties);
    }

    @Test
    @DisplayName("JWT 유틸은 토큰을 생성하고 같은 이메일을 복원한다")
    void jwtUtilCreatesAndParsesToken() {
        String token = jwtUtil.createToken("user@example.com");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("user@example.com", jwtUtil.getEmail(token));
    }

    @Test
    @DisplayName("비밀번호 인코더는 평문을 해시하고 matches로 검증한다")
    void passwordEncoderHashesPassword() {
        String encoded = passwordEncoder.encode("pw1234");

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches("pw1234", encoded));
        assertTrue(!encoded.equals("pw1234"));
    }

    @Test
    @DisplayName("CORS 설정은 프론트엔드 로컬 개발 주소를 허용한다")
    void corsConfigurationAllowsFrontendOrigins() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        CorsConfiguration configuration = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertEquals(corsProperties.getAllowedOrigins(), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(configuration.getAllowedOrigins().contains("http://127.0.0.1:5173"));
        assertTrue(configuration.getAllowedMethods().contains("POST"));
        assertTrue(Boolean.TRUE.equals(configuration.getAllowCredentials()));
    }

}
