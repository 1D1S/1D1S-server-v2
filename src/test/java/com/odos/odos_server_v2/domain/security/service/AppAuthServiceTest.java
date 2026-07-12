package com.odos.odos_server_v2.domain.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.dto.AppLoginResponse;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.oauth2.client.ProviderUserInfoClient;
import com.odos.odos_server_v2.domain.security.oauth2.service.SocialMemberService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppAuthServiceTest {

  @Mock private ProviderUserInfoClient providerUserInfoClient;
  @Mock private SocialMemberService socialMemberService;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private AppAuthService appAuthService;

  @Test
  void login_issuesTokensInBodyForResolvedMember() {
    Member member = Member.builder().id(1L).build();
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);

    when(providerUserInfoClient.fetchAttributes(SignupRoute.GOOGLE, "provider-at"))
        .thenReturn(Map.of("sub", "g-123", "email", "user@example.com"));
    when(socialMemberService.findOrCreate("user@example.com", SignupRoute.GOOGLE, "g-123"))
        .thenReturn(member);
    when(jwtTokenProvider.createAccessToken(member)).thenReturn("access");
    when(jwtTokenProvider.createRefreshToken(member)).thenReturn("refresh");
    when(jwtTokenProvider.extractExpiration("refresh"))
        .thenReturn(java.util.Optional.of(expiresAt));

    AppLoginResponse response = appAuthService.login(SignupRoute.GOOGLE, "provider-at");

    assertEquals("access", response.getAccessToken());
    assertEquals("refresh", response.getRefreshToken());
    assertFalse(response.isProfileComplete()); // 신규/미완성 프로필
    verify(refreshTokenService).saveActiveToken(eq(member), eq("refresh"), eq(expiresAt));
  }

  @Test
  void login_throwsWhenProviderHasNoEmail() {
    when(providerUserInfoClient.fetchAttributes(SignupRoute.GOOGLE, "provider-at"))
        .thenReturn(Map.of("sub", "g-123")); // email 누락

    CustomException exception =
        assertThrows(
            CustomException.class, () -> appAuthService.login(SignupRoute.GOOGLE, "provider-at"));

    assertEquals(ErrorCode.INVALID_OAUTH_EMAIL, exception.getErrorCode());
    verify(socialMemberService, never()).findOrCreate(any(), any(), any());
    verify(refreshTokenService, never()).saveActiveToken(any(), any(), any());
  }
}
