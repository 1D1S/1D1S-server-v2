package com.odos.odos_server_v2.domain.security.dto;

import lombok.Builder;
import lombok.Getter;

/** 앱 소셜 로그인 응답. 토큰을 Set-Cookie 가 아닌 JSON 바디로 내려준다. */
@Getter
@Builder
public class AppLoginResponse {
  private String accessToken;
  private String refreshToken;
  private boolean isProfileComplete;
}
