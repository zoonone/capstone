package com.capstone.backend.global.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("mysecretkeymysecretkeymysecretkey", 3600000);

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
