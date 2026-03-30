package com.capstone.backend.service;

import com.capstone.backend.dto.LoginRequest;
import com.capstone.backend.dto.SignupRequest;
import com.capstone.backend.entity.AuthProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CREDENTIALS_FAILURE_MESSAGE = "이메일 혹은 비밀번호가 잘못되었습니다.";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // 회원가입
    public User signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException(CREDENTIALS_FAILURE_MESSAGE);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        return userRepository.save(user);
    }

    // 로그인
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(CREDENTIALS_FAILURE_MESSAGE));

        if (user.getProvider() != AuthProvider.LOCAL || user.getPassword() == null) {
            throw new UnauthorizedException(CREDENTIALS_FAILURE_MESSAGE);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(CREDENTIALS_FAILURE_MESSAGE);
        }

        return jwtUtil.createToken(user.getEmail()); // ⭐ 토큰 반환
    }
}
