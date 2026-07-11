package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
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
    if (!jwtTokenProvider.isValidToken(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
    if (jwtTokenProvider.isExpired(refreshToken)) {
      throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    Member member =
        findMemberByRefreshTokenMemberId(refreshToken)
            .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

    String newAccessToken = jwtTokenProvider.createAccessToken(member);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(member);
    LocalDateTime newExpiresAt =
        jwtTokenProvider
            .extractExpiration(newRefreshToken)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

    refreshTokenService.rotate(member, refreshToken, newRefreshToken, newExpiresAt);

    return new ReissuedTokens(newAccessToken, newRefreshToken);
  }

  private Optional<Member> findMemberByRefreshTokenMemberId(String refreshToken) {
    return jwtTokenProvider
        .extractMemberId(refreshToken)
        .flatMap(
            memberId -> {
              try {
                return memberRepository.findById(Long.parseLong(memberId));
              } catch (NumberFormatException e) {
                return Optional.empty();
              }
            });
  }

  public record ReissuedTokens(String accessToken, String refreshToken) {}
}
