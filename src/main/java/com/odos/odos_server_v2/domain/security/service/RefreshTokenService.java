package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.entity.RefreshToken;
import com.odos.odos_server_v2.domain.security.repository.RefreshTokenRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public void saveActiveToken(Member member, String refreshToken, LocalDateTime expiresAt) {
    if (refreshTokenRepository.findByRefreshToken(refreshToken).isPresent()) {
      return;
    }

    // 로그인 1회 = 새 세션 → 새 family_id 를 부여한다.
    refreshTokenRepository.save(
        RefreshToken.active(member, refreshToken, expiresAt, newFamilyId()));
  }

  /**
   * RefreshToken Rotation. 제시된 이전 토큰을 검증·폐기하고 같은 family 로 새 토큰을 활성 상태로 저장한다.
   *
   * <p>이미 revoke된 토큰이 다시 제시되면 유출로 간주(재사용 감지)하여 그 토큰이 속한 family(세션)의 활성 토큰만 무효화하고 예외를 던진다. 다른 기기(다른
   * family)의 세션은 영향받지 않는다. DB에 없는 레거시 토큰은 서명이 유효한 것으로 보고 새 family 로 신규 토큰만 저장한다.
   */
  @Transactional
  public void rotate(Member member, String oldToken, String newToken, LocalDateTime newExpiresAt) {
    String familyId =
        refreshTokenRepository
            .findByRefreshToken(oldToken)
            .map(token -> rotateExistingToken(token, member.getId()))
            .orElseGet(this::newFamilyId);

    refreshTokenRepository.save(RefreshToken.active(member, newToken, newExpiresAt, familyId));
  }

  /** 검증을 통과하면 이전 토큰을 폐기하고, 새 토큰이 상속할 family_id 를 반환한다. */
  private String rotateExistingToken(RefreshToken token, Long memberId) {
    if (!token.belongsTo(memberId)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    if (token.isRevoked()) {
      // 재사용 감지: 이미 회전되어 폐기된 토큰이 다시 사용됨 → 그 family(세션)의 활성 토큰만 무효화
      refreshTokenRepository.revokeAllActiveByFamily(token.getFamilyId(), LocalDateTime.now());
      throw new CustomException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
    }

    if (token.isExpired(LocalDateTime.now())) {
      throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    token.revoke();
    return token.getFamilyId();
  }

  private String newFamilyId() {
    return UUID.randomUUID().toString();
  }

  @Transactional
  public void revokeCurrentToken(String refreshToken) {
    refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(RefreshToken::revoke);
  }
}
