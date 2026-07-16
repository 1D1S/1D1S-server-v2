package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.LOGIN_SUCCESS;

import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueRequest;
import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueResponse;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeRequest;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeResponse;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionBootstrapRequest;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionCodeResponse;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.service.NativeLoginService;
import com.odos.odos_server_v2.domain.security.service.NativeSessionService;
import com.odos.odos_server_v2.domain.security.service.RefreshTokenService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import com.odos.odos_server_v2.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/native")
@RequiredArgsConstructor
public class NativeSessionController {
  private final NativeSessionService nativeSessionService;
  private final NativeLoginService nativeLoginService;
  private final TokenReissueService tokenReissueService;
  private final RefreshTokenService refreshTokenService;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/login/exchange")
  public ApiResponse<NativeLoginExchangeResponse> exchange(
      @Valid @RequestBody NativeLoginExchangeRequest request) {
    return success(LOGIN_SUCCESS, nativeLoginService.exchange(request));
  }

  @PostMapping("/web-session-code")
  public ApiResponse<NativeSessionCodeResponse> issueWebSessionCode(
      @AuthenticationPrincipal MemberPrincipal principal) {
    requireNativePrincipal(principal);
    return success(LOGIN_SUCCESS, nativeSessionService.issueWebBootstrapCode(principal.getId()));
  }

  @PostMapping("/token/refresh")
  public ApiResponse<AppTokenReissueResponse> refresh(
      @Valid @RequestBody AppTokenReissueRequest request) {
    ReissuedTokens tokens =
        tokenReissueService.reissue(request.getRefreshToken(), SessionType.NATIVE);
    return success(
        LOGIN_SUCCESS, new AppTokenReissueResponse(tokens.accessToken(), tokens.refreshToken()));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      @AuthenticationPrincipal MemberPrincipal principal,
      @Valid @RequestBody AppTokenReissueRequest request) {
    requireNativePrincipal(principal);
    refreshTokenService.revokeCurrentToken(
        request.getRefreshToken(), principal.getId(), SessionType.NATIVE);
    return success(LOGIN_SUCCESS);
  }

  @PostMapping("/bootstrap")
  public ApiResponse<Void> bootstrap(
      @Valid @RequestBody NativeSessionBootstrapRequest request, HttpServletResponse response) {
    ReissuedTokens tokens = nativeSessionService.bootstrap(request.code());
    jwtTokenProvider.addAccessTokenCookie(response, tokens.accessToken());
    jwtTokenProvider.addRefreshTokenCookie(response, tokens.refreshToken());
    return success(LOGIN_SUCCESS);
  }

  @GetMapping("/web-session-status")
  public ApiResponse<Void> webSessionStatus(@AuthenticationPrincipal MemberPrincipal principal) {
    if (principal == null || principal.getSessionType() != SessionType.WEBVIEW) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    return success(LOGIN_SUCCESS);
  }

  private void requireNativePrincipal(MemberPrincipal principal) {
    if (principal == null || principal.getSessionType() != SessionType.NATIVE) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
  }
}
