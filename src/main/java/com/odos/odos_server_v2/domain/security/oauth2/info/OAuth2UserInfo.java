package com.odos.odos_server_v2.domain.security.oauth2.info;

import java.util.Map;

public abstract class OAuth2UserInfo {
  protected final Map<String, Object> attributes;

  public OAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public abstract String getId();

  public abstract String getEmail();
}
