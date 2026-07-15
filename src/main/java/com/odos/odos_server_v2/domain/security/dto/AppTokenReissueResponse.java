package com.odos.odos_server_v2.domain.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 앱 토큰 재발급 응답. 회전된 access/refresh 토큰을 JSON 바디로 내려준다. */
@Getter
@AllArgsConstructor
public class AppTokenReissueResponse {
  private String accessToken;
  private String refreshToken;
}
