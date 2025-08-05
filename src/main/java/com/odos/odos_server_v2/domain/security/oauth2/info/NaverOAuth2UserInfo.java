package com.odos.odos_server_v2.domain.security.oauth2.info;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {
  public NaverOAuth2UserInfo(Map<String, Object> attributes) {
    super((Map<String, Object>) attributes.get("response"));
  }

  @Override
  public String getId() {
    return String.valueOf(attributes.get("id"));
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }
}
