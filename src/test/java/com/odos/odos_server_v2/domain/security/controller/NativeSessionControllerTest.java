package com.odos.odos_server_v2.domain.security.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.service.NativeLoginService;
import com.odos.odos_server_v2.domain.security.service.NativeSessionService;
import com.odos.odos_server_v2.domain.security.service.RefreshTokenService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NativeSessionControllerTest {
  @Mock private NativeSessionService nativeSessionService;
  @Mock private NativeLoginService nativeLoginService;
  @Mock private TokenReissueService tokenReissueService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private JwtTokenProvider jwtTokenProvider;

  @InjectMocks private NativeSessionController controller;

  @Test
  void webSessionStatusAcceptsWebViewPrincipal() {
    MemberPrincipal principal = principal(SessionType.WEBVIEW);

    assertDoesNotThrow(() -> controller.webSessionStatus(principal));
  }

  @Test
  void webSessionStatusRejectsMissingOrNativePrincipal() {
    assertThrows(CustomException.class, () -> controller.webSessionStatus(null));
    assertThrows(
        CustomException.class, () -> controller.webSessionStatus(principal(SessionType.NATIVE)));
  }

  private MemberPrincipal principal(SessionType sessionType) {
    return new MemberPrincipal(
        1L, "member@example.com", "USER", SignupRoute.GOOGLE, sessionType, "session-id");
  }
}
