package com.odos.odos_server_v2.domain.security.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionBootstrapRequest;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.service.NativeLoginService;
import com.odos.odos_server_v2.domain.security.service.NativeSessionService;
import com.odos.odos_server_v2.domain.security.service.RefreshTokenService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NativeSessionControllerTest {
  @Mock private NativeSessionService nativeSessionService;
  @Mock private NativeLoginService nativeLoginService;
  @Mock private TokenReissueService tokenReissueService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private HttpServletResponse httpResponse;

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

  @Test
  void bootstrapRedirectsOnlyAfterIssuingWebViewCookies() {
    NativeSessionBootstrapRequest request =
        new NativeSessionBootstrapRequest("one-time-code", "https://dev.1day1streak.com/");
    when(nativeSessionService.bootstrap("one-time-code"))
        .thenReturn(new ReissuedTokens("access", "refresh"));

    ResponseEntity<Void> response = controller.bootstrap(request, httpResponse);

    assertEquals(HttpStatus.SEE_OTHER, response.getStatusCode());
    assertEquals("https://dev.1day1streak.com/", response.getHeaders().getLocation().toString());
    verify(jwtTokenProvider).addAccessTokenCookie(httpResponse, "access");
    verify(jwtTokenProvider).addRefreshTokenCookie(httpResponse, "refresh");
  }

  @Test
  void bootstrapRejectsExternalReturnUrlBeforeConsumingCode() {
    NativeSessionBootstrapRequest request =
        new NativeSessionBootstrapRequest("one-time-code", "https://attacker.example/");

    assertThrows(CustomException.class, () -> controller.bootstrap(request, httpResponse));

    verifyNoInteractions(nativeSessionService, jwtTokenProvider);
  }

  private MemberPrincipal principal(SessionType sessionType) {
    return new MemberPrincipal(
        1L, "member@example.com", "USER", SignupRoute.GOOGLE, sessionType, "session-id");
  }
}
