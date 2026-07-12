package com.odos.odos_server_v2.domain.security.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthTokenControllerTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private TokenReissueService tokenReissueService;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @InjectMocks private AuthTokenController authTokenController;

  @Test
  void reissueAccessToken_setsBothCookiesOnSuccess() {
    when(jwtTokenProvider.extractRefreshToken(request)).thenReturn(Optional.of("old-refresh"));
    when(tokenReissueService.reissue("old-refresh"))
        .thenReturn(new ReissuedTokens("new-access", "new-refresh"));

    authTokenController.reissueAccessToken(request, response);

    verify(jwtTokenProvider).addAccessTokenCookie(response, "new-access");
    verify(jwtTokenProvider).addRefreshTokenCookie(response, "new-refresh");
    verify(jwtTokenProvider, never()).clearTokenCookies(response);
  }

  @Test
  void reissueAccessToken_clearsCookiesWhenRefreshExpired() {
    when(jwtTokenProvider.extractRefreshToken(request)).thenReturn(Optional.of("old-refresh"));
    when(tokenReissueService.reissue("old-refresh"))
        .thenThrow(new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN));

    CustomException exception =
        assertThrows(
            CustomException.class, () -> authTokenController.reissueAccessToken(request, response));

    assertEquals(ErrorCode.EXPIRED_REFRESH_TOKEN, exception.getErrorCode());
    verify(jwtTokenProvider).clearTokenCookies(response); // 요청사항 ②: 실패 시 쿠키 제거
  }

  @Test
  void reissueAccessToken_clearsCookiesWhenRefreshMissing() {
    when(jwtTokenProvider.extractRefreshToken(request)).thenReturn(Optional.empty());

    CustomException exception =
        assertThrows(
            CustomException.class, () -> authTokenController.reissueAccessToken(request, response));

    assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    verify(jwtTokenProvider).clearTokenCookies(response);
    verify(tokenReissueService, never()).reissue(org.mockito.ArgumentMatchers.anyString());
  }
}
