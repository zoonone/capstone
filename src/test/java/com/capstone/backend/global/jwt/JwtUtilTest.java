package com.capstone.backend.global.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String TEST_JWT_SECRET = "4f9a2c7e1b6d8a0c3e5f7b9d2a4c6e8f";

    private final JwtUtil jwtUtil = new JwtUtil(TEST_JWT_SECRET, 3600000);

    @Test
    void createTokenProducesValidTokenWithEmailSubject() {
        String email = "user@example.com";

        String token = jwtUtil.createToken(email);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(email, jwtUtil.getEmail(token));
    }

    @Test
    void validateTokenReturnsFalseForTamperedToken() {
        String token = jwtUtil.createToken("user@example.com");
        String tamperedToken = token.substring(0, token.length() - 1) + "x";

        assertFalse(jwtUtil.validateToken(tamperedToken));
    }
}
