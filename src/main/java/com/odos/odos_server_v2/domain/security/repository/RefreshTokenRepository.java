package com.odos.odos_server_v2.domain.security.repository;

import com.odos.odos_server_v2.domain.security.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  @Query(
      "select r from RefreshToken r "
          + "where r.refreshToken = :fingerprint or r.refreshToken = :legacyToken")
  Optional<RefreshToken> findByFingerprintOrLegacyToken(
      @Param("fingerprint") String fingerprint, @Param("legacyToken") String legacyToken);

  // 회전 동시성 방어: 같은 토큰으로 동시 회전 요청이 오면 행 락으로 직렬화한다.
  // (선행 요청이 revoke 후 커밋되면 후행 요청은 revoked=true 를 읽어 재사용 감지로 넘어간다.)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select r from RefreshToken r "
          + "where r.refreshToken = :fingerprint or r.refreshToken = :legacyToken")
  Optional<RefreshToken> findByFingerprintOrLegacyTokenForUpdate(
      @Param("fingerprint") String fingerprint, @Param("legacyToken") String legacyToken);

  @Modifying
  @Query(
      "UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now "
          + "WHERE r.familyId = :familyId AND r.revoked = false")
  int revokeAllActiveByFamily(@Param("familyId") String familyId, @Param("now") LocalDateTime now);
}
