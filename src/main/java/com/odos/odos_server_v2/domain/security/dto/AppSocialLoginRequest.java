package com.odos.odos_server_v2.domain.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 네이티브 앱이 provider SDK 로 얻은 access_token 을 전달하는 요청 바디. */
@Getter
@NoArgsConstructor
public class AppSocialLoginRequest {

  @NotBlank(message = "provider access token 은 필수입니다.")
  private String accessToken;
}
