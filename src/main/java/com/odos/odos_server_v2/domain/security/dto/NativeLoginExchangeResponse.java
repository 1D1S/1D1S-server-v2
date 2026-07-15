package com.odos.odos_server_v2.domain.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NativeLoginExchangeResponse(
    @JsonProperty("native") NativeTokens nativeSession,
    String webBootstrapCode,
    long webBootstrapExpiresIn,
    boolean profileComplete) {

  public record NativeTokens(
      String accessToken,
      String refreshToken,
      long accessTokenExpiresIn,
      long refreshTokenExpiresIn) {}
}
