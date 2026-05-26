package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.LOGOUT;

import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.RefreshTokenService;
import com.odos.odos_server_v2.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LogoutController {
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/logout")
  public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    jwtTokenProvider
        .extractRefreshToken(request)
        .ifPresent(refreshTokenService::revokeCurrentToken);
    jwtTokenProvider.clearTokenCookies(response);

    return success(LOGOUT);
  }
}
