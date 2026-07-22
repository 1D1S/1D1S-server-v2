package com.odos.odos_server_v2.domain.push.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 앱(iOS/Android) 단말의 FCM 등록 토큰. deviceId 하나당 한 행(unique)으로 upsert 한다. */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "device_token",
    indexes = {
      @Index(name = "idx_device_token_member_active", columnList = "member_id, is_active")
    })
public class DeviceToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Column(name = "device_id", nullable = false, unique = true, length = 255)
  private String deviceId;

  @Column(nullable = false, length = 512)
  private String token;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private DevicePlatform platform;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  void touch() {
    this.updatedAt = LocalDateTime.now();
  }

  /** 같은 deviceId 재등록 시 소유자/토큰/플랫폼을 최신화한다. (기기 소유자 변경 대응) */
  public void refresh(Long memberId, String token, DevicePlatform platform) {
    this.memberId = memberId;
    this.token = token;
    this.platform = platform;
    this.isActive = true;
  }

  public void deactivate() {
    this.isActive = false;
  }
}
