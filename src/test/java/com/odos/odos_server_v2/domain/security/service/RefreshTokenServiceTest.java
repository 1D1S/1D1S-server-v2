package com.odos.odos_server_v2.domain.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.entity.RefreshToken;
import com.odos.odos_server_v2.domain.security.repository.RefreshTokenRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private RefreshTokenService refreshTokenService;

  @Captor private ArgumentCaptor<RefreshToken> tokenCaptor;

  private final Member member = Member.builder().id(1L).build();

  @Test
  void rotate_revokesOldTokenAndInheritsFamilyId() {
    LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(14);
    RefreshToken oldToken =
        RefreshToken.active(member, "old-token", LocalDateTime.now().plusDays(7), "family-A");
    when(refreshTokenRepository.findByRefreshToken("old-token")).thenReturn(Optional.of(oldToken));

    refreshTokenService.rotate(member, "old-token", "new-token", newExpiresAt);

    assertTrue(oldToken.isRevoked());
    verify(refreshTokenRepository).save(tokenCaptor.capture());
    assertEquals("family-A", tokenCaptor.getValue().getFamilyId()); // 같은 family 상속
    verify(refreshTokenRepository, never()).revokeAllActiveByFamily(any(), any());
  }

  @Test
  void rotate_startsNewFamilyForLegacyTokenNotInDb() {
    LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(14);
    when(refreshTokenRepository.findByRefreshToken("legacy-token")).thenReturn(Optional.empty());

    refreshTokenService.rotate(member, "legacy-token", "new-token", newExpiresAt);

    verify(refreshTokenRepository).save(tokenCaptor.capture());
    assertTrue(tokenCaptor.getValue().getFamilyId() != null); // 새 family 부여
    verify(refreshTokenRepository, never()).revokeAllActiveByFamily(any(), any());
  }

  @Test
  void rotate_detectsReuseAndRevokesOnlyThatFamily() {
    RefreshToken revokedToken =
        RefreshToken.active(member, "reused-token", LocalDateTime.now().plusDays(7), "family-A");
    revokedToken.revoke();
    when(refreshTokenRepository.findByRefreshToken("reused-token"))
        .thenReturn(Optional.of(revokedToken));

    CustomException exception =
        assertThrows(
            CustomException.class,
            () ->
                refreshTokenService.rotate(
                    member, "reused-token", "new-token", LocalDateTime.now().plusDays(14)));

    assertEquals(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED, exception.getErrorCode());
    // 유출된 세션(family-A)만 무효화 — 다른 기기(다른 family)는 건드리지 않는다.
    verify(refreshTokenRepository)
        .revokeAllActiveByFamily(eq("family-A"), any(LocalDateTime.class));
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  void rotate_throwsWhenTokenBelongsToAnotherMember() {
    Member otherOwner = Member.builder().id(2L).build();
    RefreshToken othersToken =
        RefreshToken.active(
            otherOwner, "others-token", LocalDateTime.now().plusDays(7), "family-B");
    when(refreshTokenRepository.findByRefreshToken("others-token"))
        .thenReturn(Optional.of(othersToken));

    CustomException exception =
        assertThrows(
            CustomException.class,
            () ->
                refreshTokenService.rotate(
                    member, "others-token", "new-token", LocalDateTime.now().plusDays(14)));

    assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  void rotate_throwsWhenStoredTokenExpired() {
    RefreshToken expiredToken =
        RefreshToken.active(
            member, "expired-token", LocalDateTime.now().minusMinutes(1), "family-A");
    when(refreshTokenRepository.findByRefreshToken("expired-token"))
        .thenReturn(Optional.of(expiredToken));

    CustomException exception =
        assertThrows(
            CustomException.class,
            () ->
                refreshTokenService.rotate(
                    member, "expired-token", "new-token", LocalDateTime.now().plusDays(14)));

    assertEquals(ErrorCode.EXPIRED_REFRESH_TOKEN, exception.getErrorCode());
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
  }
}
