package com.odos.odos_server_v2.domain.security.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.security.dto.AppLoginResponse;
import com.odos.odos_server_v2.domain.security.dto.AppSocialLoginRequest;
import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueRequest;
import com.odos.odos_server_v2.domain.security.dto.AppTokenReissueResponse;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.AppAuthService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
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

  @Mock private AppAuthService appAuthService;
  @Mock private TokenReissueService tokenReissueService;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private HttpServletRequest httpRequest;

  @InjectMocks private AppAuthController appAuthController;

  @Test
  void login_delegatesToServiceForKnownProvider() {
    AppSocialLoginRequest request = new AppSocialLoginRequest();
    ReflectionTestUtils.setField(request, "accessToken", "provider-at");
    AppLoginResponse serviceResponse =
        AppLoginResponse.builder()
            .accessToken("a")
            .refreshToken("r")
            .isProfileComplete(true)
            .build();
    when(appAuthService.login(SignupRoute.KAKAO, "provider-at")).thenReturn(serviceResponse);

    ApiResponse<AppLoginResponse> response = appAuthController.login("kakao", request);

    assertEquals(serviceResponse, response.getData());
    verify(appAuthService).login(SignupRoute.KAKAO, "provider-at");
  }

  @Test
  void login_rejectsUnknownProvider() {
    AppSocialLoginRequest request = new AppSocialLoginRequest();
    ReflectionTestUtils.setField(request, "accessToken", "provider-at");

    CustomException exception =
        assertThrows(CustomException.class, () -> appAuthController.login("wrong", request));

    assertEquals(ErrorCode.INVALID_SIGNUP_PROVIDER, exception.getErrorCode());
    verify(appAuthService, never()).login(any(), anyString());
  }

  @Test
  void reissue_usesRefreshTokenFromBody() {
    AppTokenReissueRequest request = new AppTokenReissueRequest();
    ReflectionTestUtils.setField(request, "refreshToken", "body-refresh");
    when(tokenReissueService.reissue("body-refresh"))
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
    when(tokenReissueService.reissue("header-refresh"))
        .thenReturn(new ReissuedTokens("new-access", "new-refresh"));

    ApiResponse<AppTokenReissueResponse> response = appAuthController.reissue(httpRequest, null);

    assertEquals("new-access", response.getData().getAccessToken());
    verify(tokenReissueService).reissue("header-refresh");
  }
}
