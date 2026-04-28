package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "notification_endpoint",
    indexes = {
      @Index(name = "idx_notification_endpoint_member_active", columnList = "member_id, is_active")
    })
public class NotificationEndpoint extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "endpoint_url", length = 1024)
  private String endpointUrl;

  @Column(name = "p256dh", length = 512)
  private String p256dh;

  @Column(name = "auth_secret", length = 512)
  private String authSecret;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "last_seen_at")
  private LocalDateTime lastSeenAt;

  // 후반에 추가
  @Builder.Default
  @Column(nullable = false)
  private Boolean enabled = true;

  public void disable() {
    this.enabled = false;
  }

  public void updateWebSubscription(String endpointUrl, String p256dh, String authSecret) {
    this.endpointUrl = endpointUrl;
    this.p256dh = p256dh;
    this.authSecret = authSecret;
    this.isActive = true;
    this.lastSeenAt = LocalDateTime.now();
  }

  public void deactivate() {
    this.isActive = false;
  }
}
