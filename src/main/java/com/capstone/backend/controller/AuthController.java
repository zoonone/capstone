package com.capstone.backend.controller;

import com.capstone.backend.dto.LoginRequest;
import com.capstone.backend.dto.SignupRequest;
import com.capstone.backend.dto.TokenResponse;
import jakarta.validation.Valid;
import com.capstone.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Set<String> SUPPORTED_OAUTH_PROVIDERS = Set.of("google", "naver");

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new TokenResponse(authService.login(request)));
    }

    @GetMapping("/me")
    public String me(Authentication auth) {
        if (auth == null) {
            return "";
        }
        return auth.getName();
    }

    @GetMapping("/oauth2/url/{provider}")
    public ResponseEntity<String> getOauth2AuthorizationUrl(@PathVariable String provider) {
        String normalizedProvider = provider.toLowerCase();
        if (!SUPPORTED_OAUTH_PROVIDERS.contains(normalizedProvider)) {
            throw new IllegalArgumentException("지원하지 않는 OAuth provider입니다.");
        }
        return ResponseEntity.ok("/oauth2/authorization/" + normalizedProvider);
    }
}
