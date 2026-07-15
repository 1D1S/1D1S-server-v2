package com.odos.odos_server_v2.domain.security.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueRequest;
import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueResponse;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AppAuthControllerTest {

  @Mock private TokenReissueService tokenReissueService;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private HttpServletRequest httpRequest;

  @InjectMocks private AppAuthController appAuthController;

  @Test
  void reissue_usesRefreshTokenFromBody() {
    AppTokenReissueRequest request = new AppTokenReissueRequest();
    ReflectionTestUtils.setField(request, "refreshToken", "body-refresh");
    when(tokenReissueService.reissue("body-refresh", SessionType.NATIVE))
        .thenReturn(new ReissuedTokens("new-access", "new-refresh"));

    ApiResponse<AppTokenReissueResponse> response = appAuthController.reissue(httpRequest, request);

    assertEquals("new-access", response.getData().getAccessToken());
    assertEquals("new-refresh", response.getData().getRefreshToken());
    verify(jwtTokenProvider, never()).extractRefreshToken(any());
  }

  @Test
  void reissue_fallsBackToHeaderWhenBodyEmpty() {
    when(jwtTokenProvider.extractRefreshToken(httpRequest))
        .thenReturn(java.util.Optional.of("header-refresh"));
    when(tokenReissueService.reissue("header-refresh", SessionType.NATIVE))
        .thenReturn(new ReissuedTokens("new-access", "new-refresh"));

    ApiResponse<AppTokenReissueResponse> response = appAuthController.reissue(httpRequest, null);

    assertEquals("new-access", response.getData().getAccessToken());
    verify(tokenReissueService).reissue("header-refresh", SessionType.NATIVE);
  }
}
