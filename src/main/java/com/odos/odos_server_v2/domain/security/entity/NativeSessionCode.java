package com.odos.odos_server_v2.domain.security.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "native_session_code")
public class NativeSessionCode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "code_hash", nullable = false, unique = true, length = 64)
  private String codeHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = false, length = 32)
  private NativeSessionCodePurpose purpose;

  @Column(name = "code_challenge", length = 43)
  private String codeChallenge;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "used_at")
  private LocalDateTime usedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public static NativeSessionCode issue(
      Member member,
      String codeHash,
      NativeSessionCodePurpose purpose,
      String codeChallenge,
      LocalDateTime now,
      LocalDateTime expiresAt) {
    return NativeSessionCode.builder()
        .member(member)
        .codeHash(codeHash)
        .purpose(purpose)
        .codeChallenge(codeChallenge)
        .createdAt(now)
        .expiresAt(expiresAt)
        .build();
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public boolean isExpired(LocalDateTime now) {
    return !expiresAt.isAfter(now);
  }

  public void use(LocalDateTime now) {
    usedAt = now;
  }
}
