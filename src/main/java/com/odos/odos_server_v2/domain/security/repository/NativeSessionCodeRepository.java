package com.odos.odos_server_v2.domain.security.repository;

import com.odos.odos_server_v2.domain.security.entity.NativeSessionCode;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NativeSessionCodeRepository extends JpaRepository<NativeSessionCode, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from NativeSessionCode c join fetch c.member where c.codeHash = :codeHash")
  Optional<NativeSessionCode> findByCodeHashForUpdate(@Param("codeHash") String codeHash);

  long deleteByExpiresAtBefore(LocalDateTime cutoff);
}
