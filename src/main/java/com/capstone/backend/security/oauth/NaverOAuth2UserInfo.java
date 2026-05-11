package com.capstone.backend.security.oauth;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> response;

    @SuppressWarnings("unchecked")
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProviderId() {
        return response == null ? null : (String) response.get("id");
    }

    @Override
    public String getEmail() {
        return response == null ? null : (String) response.get("email");
    }

    @Override
    public String getName() {
        if (response == null) {
            return null;
        }
        String name = (String) response.get("name");
        return name != null ? name : (String) response.get("nickname");
    }
}
