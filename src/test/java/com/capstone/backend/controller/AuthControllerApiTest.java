package com.capstone.backend.controller;

import com.capstone.backend.dto.SignupRequest;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.global.exception.GlobalExceptionHandler;
import com.capstone.backend.global.exception.UnauthorizedException;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.SecurityConfig;
import com.capstone.backend.security.oauth.OAuth2AuthenticationFailureHandler;
import com.capstone.backend.security.oauth.CustomOAuth2UserService;
import com.capstone.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
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

@WebMvcTest(
        value = AuthController.class,
        properties = "jwt.secret=4f9a2c7e1b6d8a0c3e5f7b9d2a4c6e8f"
)
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, GlobalExceptionHandler.class})
class AuthControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

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
    void signupReturnsConflictForDuplicateEmail() throws Exception {
        when(authService.signup(any(SignupRequest.class))).thenThrow(new ConflictException("이메일 혹은 비밀번호가 잘못되었습니다."));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "password": "pw1234",
                                  "name": "New User"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호가 잘못되었습니다."));
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new UnauthorizedException("이메일 혹은 비밀번호가 잘못되었습니다."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "pw1234"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호가 잘못되었습니다."));
    }

    @Test
    void loginReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호가 잘못되었습니다."));
    }

    @Test
    void meReturnsOkWithoutTokenWhenSecurityIsOpenForFrontendDevelopment() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
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
                .andExpect(jsonPath("$.message").value("이메일 혹은 비밀번호가 잘못되었습니다."));
    }

    @Test
    void oauth2UrlReturnsAuthorizationPathForSupportedProvider() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/url/google"))
                .andExpect(status().isOk())
                .andExpect(content().string("/oauth2/authorization/google"));
    }

    @Test
    void oauth2UrlReturnsBadRequestForUnsupportedProvider() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/url/kakao"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("지원하지 않는 OAuth provider입니다."));
    }
}
