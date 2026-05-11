package com.capstone.backend.security.oauth;

import com.capstone.backend.entity.AuthProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
        return upsertOAuth2User(registrationId, userInfo, oauth2User.getAttributes());
    }

    OAuth2User upsertOAuth2User(String registrationId, OAuth2UserInfo userInfo, java.util.Map<String, Object> attributes) {
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> userRepository.findByEmail(userInfo.getEmail())
                        .map(existingUser -> updateExistingUser(existingUser, provider, userInfo))
                        .orElseGet(() -> createOAuthUser(provider, userInfo)));
        return new CustomOAuth2User(user.getEmail(), attributes);
    }

    private User updateExistingUser(User user, AuthProvider provider, OAuth2UserInfo userInfo) {
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
}
