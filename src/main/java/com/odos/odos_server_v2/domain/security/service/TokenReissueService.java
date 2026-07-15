package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RefreshToken 으로 access/refresh 토큰을 회전(RTR)해 재발급하는 공용 로직. 웹(쿠키)과 앱(JSON 바디) 컨트롤러가 전달 방식만 다르게 하여 이
 * 서비스를 공유한다.
 */
@Service
@RequiredArgsConstructor
public class TokenReissueService {
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public ReissuedTokens reissue(String refreshToken) {
    return reissue(refreshToken, null);
  }

  @Transactional
  public ReissuedTokens reissue(String refreshToken, SessionType expectedSessionType) {
    Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);

    Member member =
        findMemberByRefreshTokenMemberId(claims)
            .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

    SessionType sessionType = parseSessionType(claims);
    if (expectedSessionType != null && sessionType != expectedSessionType) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
    String sessionId = claims.get("sid", String.class);
    String newAccessToken = jwtTokenProvider.createAccessToken(member, sessionType, sessionId);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(member, sessionType, sessionId);
    LocalDateTime newExpiresAt =
        jwtTokenProvider
            .extractExpiration(newRefreshToken)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

    refreshTokenService.rotate(member, refreshToken, newRefreshToken, newExpiresAt);

    return new ReissuedTokens(newAccessToken, newRefreshToken);
  }

  private java.util.Optional<Member> findMemberByRefreshTokenMemberId(Claims claims) {
    Object memberId = claims.get("id");
    if (memberId == null) {
      return java.util.Optional.empty();
    }
    try {
      return memberRepository.findById(Long.parseLong(memberId.toString()));
    } catch (NumberFormatException e) {
      return java.util.Optional.empty();
    }
  }

  private SessionType parseSessionType(Claims claims) {
    String value = claims.get("session_type", String.class);
    if (value == null) {
      return SessionType.WEBVIEW;
    }
    try {
      return SessionType.valueOf(value);
    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
  }

  public record ReissuedTokens(String accessToken, String refreshToken) {}
}
