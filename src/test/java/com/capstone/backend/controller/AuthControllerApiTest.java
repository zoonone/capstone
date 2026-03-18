package com.capstone.backend.controller;

import com.capstone.backend.dto.SignupRequest;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.GlobalExceptionHandler;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.SecurityConfig;
import com.capstone.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, GlobalExceptionHandler.class})
class AuthControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    @Test
    void signupReturnsCreated() throws Exception {
        when(authService.signup(any(SignupRequest.class))).thenReturn(User.builder().build());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "password": "pw1234",
                                  "name": "New User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("회원가입 성공"));
    }

    @Test
    void loginReturnsAccessTokenJson() throws Exception {
        String token = jwtUtil.createToken("user@example.com");
        when(authService.login(argThat(request ->
                "user@example.com".equals(request.getEmail()) &&
                        "pw1234".equals(request.getPassword())))).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "pw1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(token));
    }

    @Test
    void meReturnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsAuthenticatedEmailWithValidToken() throws Exception {
        String token = jwtUtil.createToken("user@example.com");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("user@example.com"));
    }

    @Test
    void signupReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": "123",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
