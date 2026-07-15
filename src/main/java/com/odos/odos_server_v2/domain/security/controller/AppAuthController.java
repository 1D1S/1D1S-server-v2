package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.TOKEN_REFRESH;

import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueRequest;
import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueResponse;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import com.odos.odos_server_v2.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 네이티브 앱 전용 인증 API. 토큰을 쿠키가 아닌 JSON 바디로 주고받는다. */
@RestController
@RequestMapping("/auth/app")
@RequiredArgsConstructor
public class AppAuthController {
  private final TokenReissueService tokenReissueService;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/token")
  public ApiResponse<AppTokenReissueResponse> reissue(
      HttpServletRequest httpRequest,
      @RequestBody(required = false) AppTokenReissueRequest request) {
    String refreshToken = resolveRefreshToken(httpRequest, request);
    ReissuedTokens tokens = tokenReissueService.reissue(refreshToken, SessionType.NATIVE);
    return success(
        TOKEN_REFRESH, new AppTokenReissueResponse(tokens.accessToken(), tokens.refreshToken()));
  }

  private String resolveRefreshToken(
      HttpServletRequest httpRequest, AppTokenReissueRequest request) {
    if (request != null
        && request.getRefreshToken() != null
        && !request.getRefreshToken().isBlank()) {
      return request.getRefreshToken();
    }
    return jwtTokenProvider
        .extractRefreshToken(httpRequest)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
  }
}
