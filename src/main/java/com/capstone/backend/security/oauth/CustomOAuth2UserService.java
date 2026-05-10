package com.capstone.backend.security.oauth;

import com.capstone.backend.entity.AuthProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.global.exception.ConflictException;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String OAUTH2_EMAIL_REQUIRED_MESSAGE = "OAuth 계정에서 이메일 정보를 가져오지 못했습니다.";
    private static final String OAUTH2_PROVIDER_ID_REQUIRED_MESSAGE = "OAuth 계정 식별 정보를 가져오지 못했습니다.";
    private static final String OAUTH2_ACCOUNT_CONFLICT_MESSAGE = "동일한 이메일로 가입된 일반 계정이 있습니다. 일반 로그인으로 이용해주세요.";

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
        return upsertOAuth2User(registrationId, userInfo, oauth2User.getAttributes());
    }

    @Transactional
    OAuth2User upsertOAuth2User(String registrationId, OAuth2UserInfo userInfo, java.util.Map<String, Object> attributes) {
        validateRequiredUserInfo(userInfo);
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> userRepository.findByEmail(userInfo.getEmail())
                        .map(existingUser -> updateExistingUser(existingUser, provider, userInfo))
                        .orElseGet(() -> createOAuthUser(provider, userInfo)));
        return new CustomOAuth2User(user.getEmail(), attributes);
    }

    private User updateExistingUser(User user, AuthProvider provider, OAuth2UserInfo userInfo) {
        if (user.getProvider() != provider) {
            throw new ConflictException(OAUTH2_ACCOUNT_CONFLICT_MESSAGE);
        }
        user.setName(userInfo.getName());
        user.setProvider(provider);
        user.setProviderId(userInfo.getProviderId());
        return userRepository.save(user);
    }

    private User createOAuthUser(AuthProvider provider, OAuth2UserInfo userInfo) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .provider(provider)
                .providerId(userInfo.getProviderId())
                .build();
        return userRepository.save(user);
    }

    private void validateRequiredUserInfo(OAuth2UserInfo userInfo) {
        if (isBlank(userInfo.getProviderId())) {
            throw new InternalAuthenticationServiceException(OAUTH2_PROVIDER_ID_REQUIRED_MESSAGE);
        }
        if (isBlank(userInfo.getEmail())) {
            throw new InternalAuthenticationServiceException(OAUTH2_EMAIL_REQUIRED_MESSAGE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
