package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionTokenIssuer {
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  public IssuedSession issue(Member member, SessionType sessionType) {
    String sessionId = UUID.randomUUID().toString();
    String accessToken = jwtTokenProvider.createAccessToken(member, sessionType, sessionId);
    String refreshToken = jwtTokenProvider.createRefreshToken(member, sessionType, sessionId);
    LocalDateTime refreshExpiresAt =
        jwtTokenProvider
            .extractExpiration(refreshToken)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
    refreshTokenService.saveActiveToken(
        member, refreshToken, refreshExpiresAt, sessionId, sessionType);
    return new IssuedSession(sessionId, accessToken, refreshToken);
  }

  public record IssuedSession(String sessionId, String accessToken, String refreshToken) {}
}
