package com.odos.odos_server_v2.domain.security.controller;

import static org.mockito.Mockito.verify;

import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutControllerTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private HttpServletResponse response;

  @InjectMocks private LogoutController logoutController;

  @Test
  void logout_clearsOnlyCurrentClientCookies() {
    logoutController.logout(response);

    verify(jwtTokenProvider).clearTokenCookies(response);
  }
}
