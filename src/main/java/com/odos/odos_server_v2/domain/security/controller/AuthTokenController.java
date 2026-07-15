package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.TOKEN_REFRESH;

import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import com.odos.odos_server_v2.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthTokenController {
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenReissueService tokenReissueService;

  @GetMapping("/token")
  public ApiResponse<Void> reissueAccessToken(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      String refreshToken =
          jwtTokenProvider
              .extractRefreshToken(request)
              .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

      ReissuedTokens tokens = tokenReissueService.reissue(refreshToken, SessionType.WEBVIEW);

      jwtTokenProvider.addAccessTokenCookie(response, tokens.accessToken());
      jwtTokenProvider.addRefreshTokenCookie(response, tokens.refreshToken());

      return success(TOKEN_REFRESH);
    } catch (CustomException e) {
      // 재발급 실패(만료/무효/재사용 감지) 시 남아있는 토큰 쿠키를 제거해 클라이언트 상태를 초기화한다.
      jwtTokenProvider.clearTokenCookies(response);
      throw e;
    }
  }
}
