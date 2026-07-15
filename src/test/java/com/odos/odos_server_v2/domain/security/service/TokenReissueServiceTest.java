package com.odos.odos_server_v2.domain.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenReissueServiceTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private MemberRepository memberRepository;
  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private TokenReissueService tokenReissueService;

  @Test
  void reissue_rotatesAndReturnsNewTokens() {
    LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(14);
    Member member = Member.builder().id(1L).build();

    Claims claims = Jwts.claims();
    claims.put("id", 1L);
    when(jwtTokenProvider.parseRefreshToken("old-refresh")).thenReturn(claims);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    when(jwtTokenProvider.createAccessToken(member, SessionType.WEBVIEW, null))
        .thenReturn("new-access");
    when(jwtTokenProvider.createRefreshToken(member, SessionType.WEBVIEW, null))
        .thenReturn("new-refresh");
    when(jwtTokenProvider.extractExpiration("new-refresh")).thenReturn(Optional.of(newExpiresAt));

    ReissuedTokens tokens = tokenReissueService.reissue("old-refresh");

    assertEquals("new-access", tokens.accessToken());
    assertEquals("new-refresh", tokens.refreshToken());
    verify(refreshTokenService).rotate(member, "old-refresh", "new-refresh", newExpiresAt);
  }

  @Test
  void reissue_throwsWhenSignatureInvalid() {
    when(jwtTokenProvider.parseRefreshToken("bad"))
        .thenThrow(new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

    CustomException exception =
        assertThrows(CustomException.class, () -> tokenReissueService.reissue("bad"));

    assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    verify(refreshTokenService, never()).rotate(any(), any(), any(), any());
  }

  @Test
  void reissue_throwsWhenExpired() {
    when(jwtTokenProvider.parseRefreshToken("old-refresh"))
        .thenThrow(new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN));

    CustomException exception =
        assertThrows(CustomException.class, () -> tokenReissueService.reissue("old-refresh"));

    assertEquals(ErrorCode.EXPIRED_REFRESH_TOKEN, exception.getErrorCode());
  }

  @Test
  void reissue_throwsWhenMemberNotFound() {
    Claims claims = Jwts.claims();
    claims.put("id", 1L);
    when(jwtTokenProvider.parseRefreshToken("old-refresh")).thenReturn(claims);
    when(memberRepository.findById(1L)).thenReturn(Optional.empty());

    CustomException exception =
        assertThrows(CustomException.class, () -> tokenReissueService.reissue("old-refresh"));

    assertEquals(ErrorCode.REFRESH_TOKEN_NOT_FOUND, exception.getErrorCode());
  }
}
