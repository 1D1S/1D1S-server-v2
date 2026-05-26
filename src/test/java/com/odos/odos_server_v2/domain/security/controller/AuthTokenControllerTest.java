package com.odos.odos_server_v2.domain.security.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
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
  @Mock private MemberRepository memberRepository;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @InjectMocks private AuthTokenController authTokenController;

  @Test
  void reissueAccessToken_usesRefreshTokenMemberIdWithoutStoredTokenMatch() {
    String refreshToken = "valid-refresh-token";
    String accessToken = "new-access-token";
    String newRefreshToken = "new-refresh-token";
    Member member = Member.builder().id(1L).build();

    when(jwtTokenProvider.extractRefreshToken(request)).thenReturn(Optional.of(refreshToken));
    when(jwtTokenProvider.isValidToken(refreshToken)).thenReturn(true);
    when(jwtTokenProvider.isExpired(refreshToken)).thenReturn(false);
    when(jwtTokenProvider.extractMemberId(refreshToken)).thenReturn(Optional.of("1"));
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    when(jwtTokenProvider.createAccessToken(member)).thenReturn(accessToken);
    when(jwtTokenProvider.createRefreshToken(member)).thenReturn(newRefreshToken);

    authTokenController.reissueAccessToken(request, response);

    verify(jwtTokenProvider).sendAccessAndRefreshToken(response, accessToken, newRefreshToken);
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
