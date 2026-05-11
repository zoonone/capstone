package com.capstone.backend.security.oauth;

public interface OAuth2UserInfo {

    String getProviderId();

    String getEmail();

    String getName();
}
