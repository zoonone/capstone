package com.capstone.backend.security.oauth;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id == null ? null : String.valueOf(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        return account == null ? null : (String) account.get("email");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String nickname = properties == null ? null : (String) properties.get("nickname");
        return nickname != null ? nickname : getEmail();
    }
}
