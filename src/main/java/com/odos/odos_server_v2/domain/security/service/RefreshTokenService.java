package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.entity.RefreshToken;
import com.odos.odos_server_v2.domain.security.repository.RefreshTokenRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
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

    refreshTokenRepository.save(RefreshToken.active(member, refreshToken, expiresAt));
  }

  @Transactional
  public void validateOrRegisterMigrationToken(
      Member member, String refreshToken, LocalDateTime expiresAt) {
    refreshTokenRepository
        .findByRefreshToken(refreshToken)
        .ifPresentOrElse(
            token -> validateExistingToken(token, member.getId()),
            () ->
                refreshTokenRepository.save(RefreshToken.active(member, refreshToken, expiresAt)));
  }

  private void validateExistingToken(RefreshToken token, Long memberId) {
    if (!token.belongsTo(memberId)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    if (token.isRevoked()) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    if (token.isExpired(LocalDateTime.now())) {
      throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }
  }

  @Transactional
  public void revokeCurrentToken(String refreshToken) {
    refreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(RefreshToken::revoke);
  }
}
