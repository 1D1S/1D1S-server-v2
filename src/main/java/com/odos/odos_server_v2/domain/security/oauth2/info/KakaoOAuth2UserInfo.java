package com.odos.odos_server_v2.domain.security.oauth2.info;

import java.util.Map;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {
    private final Map<String, Object> account;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
        this.account = (Map<String, Object>) attributes.get("kakao_account");
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        return (account != null) ? (String) account.get("email") : null;
    }
}
