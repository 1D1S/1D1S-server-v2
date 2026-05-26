package com.odos.odos_server_v2.domain.security.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutControllerTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @InjectMocks private LogoutController logoutController;

  @Test
  void logout_revokesCurrentRefreshTokenAndClearsCookies() {
    String refreshToken = "current-refresh-token";
    when(jwtTokenProvider.extractRefreshToken(request)).thenReturn(Optional.of(refreshToken));

    logoutController.logout(request, response);

    verify(refreshTokenService).revokeCurrentToken(refreshToken);
    verify(jwtTokenProvider).clearTokenCookies(response);
  }
}
