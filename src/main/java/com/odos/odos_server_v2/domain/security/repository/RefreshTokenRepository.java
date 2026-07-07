package com.odos.odos_server_v2.domain.security.repository;

import com.odos.odos_server_v2.domain.security.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByRefreshToken(String refreshToken);

  @Modifying
  @Query(
      "UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now "
          + "WHERE r.familyId = :familyId AND r.revoked = false")
  int revokeAllActiveByFamily(@Param("familyId") String familyId, @Param("now") LocalDateTime now);
}
