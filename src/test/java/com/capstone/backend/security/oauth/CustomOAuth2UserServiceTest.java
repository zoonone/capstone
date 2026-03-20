package com.capstone.backend.security.oauth;

import com.capstone.backend.entity.AuthProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void createsGoogleUserWhenEmailDoesNotExist() {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google",
                Map.of("sub", "google-123", "email", "user@example.com", "name", "Google User"));

        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User user = customOAuth2UserService.upsertOAuth2User("google", userInfo, Map.of("email", "user@example.com"));

        assertEquals("user@example.com", user.getName());
    }

    @Test
    void createsKakaoUserFromNestedAttributes() {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("kakao",
                Map.of(
                        "id", 123456789L,
                        "kakao_account", Map.of("email", "kakao@example.com"),
                        "properties", Map.of("nickname", "Kakao User")
                ));

        when(userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "123456789")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("kakao@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User user = customOAuth2UserService.upsertOAuth2User("kakao", userInfo, Map.of("id", 123456789L));

        assertEquals("kakao@example.com", user.getName());
    }

    @Test
    void createsNaverUserFromResponseAttributes() {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("naver",
                Map.of("response", Map.of(
                        "id", "naver-123",
                        "email", "naver@example.com",
                        "name", "Naver User"
                )));

        when(userRepository.findByProviderAndProviderId(AuthProvider.NAVER, "naver-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("naver@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User user = customOAuth2UserService.upsertOAuth2User("naver", userInfo, Map.of("response", Map.of("id", "naver-123")));

        assertEquals("naver@example.com", user.getName());
    }
}
