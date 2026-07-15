package com.odos.odos_server_v2.domain.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {
  private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

  private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
  private final Member member =
      Member.builder()
          .id(1L)
          .email("member@example.com")
          .role(MemberRole.USER)
          .signupRoute(SignupRoute.KAKAO)
          .build();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", SECRET);
    ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpirationPeriod", 60_000L);
    ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationPeriod", 120_000L);
  }

  @Test
  void accessTokenCannotBeUsedAsRefreshToken() {
    String token = jwtTokenProvider.createAccessToken(member);

    CustomException exception =
        assertThrows(CustomException.class, () -> jwtTokenProvider.parseRefreshToken(token));

    assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
  }

  @Test
  void refreshTokenCannotBeUsedAsAccessToken() {
    String token = jwtTokenProvider.createRefreshToken(member);

    CustomException exception =
        assertThrows(CustomException.class, () -> jwtTokenProvider.parseAccessToken(token));

    assertEquals(ErrorCode.INVALID_JWT, exception.getErrorCode());
  }

  @Test
  void typedParsersAcceptTheirOwnTokenType() {
    String accessToken = jwtTokenProvider.createAccessToken(member);
    String refreshToken = jwtTokenProvider.createRefreshToken(member);

    assertEquals(
        JwtTokenProvider.ACCESS_TOKEN_SUBJECT,
        jwtTokenProvider.parseAccessToken(accessToken).getSubject());
    assertEquals(
        JwtTokenProvider.REFRESH_TOKEN_SUBJECT,
        jwtTokenProvider.parseRefreshToken(refreshToken).getSubject());
  }
}
