package com.odos.odos_server_v2.domain.security.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "refresh_token")
public class RefreshToken extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "refresh_token", nullable = false, unique = true, length = 512)
  private String refreshToken;

  @Column(name = "family_id", nullable = false, length = 36)
  private String familyId;

  @Column(nullable = false)
  private boolean revoked;

  private LocalDateTime revokedAt;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  public static RefreshToken active(
      Member member, String refreshToken, LocalDateTime expiresAt, String familyId) {
    return RefreshToken.builder()
        .member(member)
        .refreshToken(refreshToken)
        .familyId(familyId)
        .revoked(false)
        .expiresAt(expiresAt)
        .build();
  }

  public boolean belongsTo(Long memberId) {
    return member != null && member.getId().equals(memberId);
  }

  public boolean isExpired(LocalDateTime now) {
    return !expiresAt.isAfter(now);
  }

  public void revoke() {
    if (!revoked) {
      revoked = true;
      revokedAt = LocalDateTime.now();
    }
  }
}
