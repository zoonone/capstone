package com.capstone.backend.controller;

import com.capstone.backend.global.exception.GlobalExceptionHandler;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.global.jwt.JwtUtil;
import com.capstone.backend.security.SecurityConfig;
import com.capstone.backend.security.oauth.CustomOAuth2UserService;
import com.capstone.backend.security.oauth.OAuth2AuthenticationFailureHandler;
import com.capstone.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = HomeController.class,
        properties = "jwt.secret=4f9a2c7e1b6d8a0c3e5f7b9d2a4c6e8f"
)
@Import({SecurityConfig.class, JwtFilter.class, JwtUtil.class, GlobalExceptionHandler.class})
class HomeControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Test
    void rootReturnsOkWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.message").value("Backend server is running"));
    }

    @Test
    void preflightRequestIsAllowedForFrontendDevServer() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }
}
