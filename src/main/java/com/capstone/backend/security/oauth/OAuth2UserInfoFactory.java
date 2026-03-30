package com.capstone.backend.security.oauth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        if ("kakao".equalsIgnoreCase(registrationId)) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        if ("naver".equalsIgnoreCase(registrationId)) {
            return new NaverOAuth2UserInfo(attributes);
        }

        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                "지원하지 않는 OAuth provider입니다: " + registrationId);
    }
}
