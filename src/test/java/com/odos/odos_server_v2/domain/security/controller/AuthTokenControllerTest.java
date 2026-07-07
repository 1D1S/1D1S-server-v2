package com.odos.odos_server_v2.domain.security.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.service.RefreshTokenService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthTokenControllerTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private MemberRepository memberRepository;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @InjectMocks private AuthTokenController authTokenController;

  @Test
  void reissueAccessToken_rotatesRefreshTokenAndReissuesBothTokens() {
    String oldRefreshToken = "valid-refresh-token";
    String newRefreshToken = "new-refresh-token";
    String accessToken = "new-access-token";
    LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(14);
    Member member = Member.builder().id(1L).build();

    when(jwtTokenProvider.extractRefreshToken(request)).thenReturn(Optional.of(oldRefreshToken));
    when(jwtTokenProvider.isValidToken(oldRefreshToken)).thenReturn(true);
    when(jwtTokenProvider.isExpired(oldRefreshToken)).thenReturn(false);
    when(jwtTokenProvider.extractMemberId(oldRefreshToken)).thenReturn(Optional.of("1"));
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    when(jwtTokenProvider.createAccessToken(member)).thenReturn(accessToken);
    when(jwtTokenProvider.createRefreshToken(member)).thenReturn(newRefreshToken);
    when(jwtTokenProvider.extractExpiration(newRefreshToken)).thenReturn(Optional.of(newExpiresAt));

    authTokenController.reissueAccessToken(request, response);

    verify(refreshTokenService).rotate(member, oldRefreshToken, newRefreshToken, newExpiresAt);
    verify(jwtTokenProvider).addAccessTokenCookie(response, accessToken);
    verify(jwtTokenProvider).addRefreshTokenCookie(response, newRefreshToken);
  }

  @Test
  void reissueAccessToken_throwsWhenRefreshTokenMemberDoesNotExist() {
    String refreshToken = "valid-refresh-token";

    when(jwtTokenProvider.extractRefreshToken(request)).thenReturn(Optional.of(refreshToken));
    when(jwtTokenProvider.isValidToken(refreshToken)).thenReturn(true);
    when(jwtTokenProvider.isExpired(refreshToken)).thenReturn(false);
    when(jwtTokenProvider.extractMemberId(refreshToken)).thenReturn(Optional.of("1"));
    when(memberRepository.findById(1L)).thenReturn(Optional.empty());

    CustomException exception =
        assertThrows(
            CustomException.class, () -> authTokenController.reissueAccessToken(request, response));

    assertEquals(ErrorCode.REFRESH_TOKEN_NOT_FOUND, exception.getErrorCode());
  }
}
