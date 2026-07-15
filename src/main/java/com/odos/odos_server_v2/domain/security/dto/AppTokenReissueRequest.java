package com.odos.odos_server_v2.domain.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 앱 토큰 재발급 요청 바디. 바디가 비어 있으면 Authorization-Refresh 헤더로 폴백한다. */
@Getter
@NoArgsConstructor
public class AppTokenReissueRequest {
  @NotBlank private String refreshToken;
}
