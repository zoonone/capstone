package com.capstone.backend.security;

import com.capstone.backend.global.jwt.JwtFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @PostConstruct
    public void init() {
        System.out.println("🔥 SecurityConfig 로드됨");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())

                // 🔥 기본 로그인 완전 차단
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 🔥 JWT 필수 설정
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/test").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    //
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new RuntimeException("UserDetailsService not used");
        };
    }
}
