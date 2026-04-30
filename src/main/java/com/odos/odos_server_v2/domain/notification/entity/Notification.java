package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 알림 수신자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private Member receiver;

  // 알림 발송자 (느슨한 결합 사용)
  @Column(name = "sender_id")
  private Long senderId;

  // 알림 대상의 ID (예: 일지 ID, 챌린지 ID 등 - 느슨한 결합 사용)
  @Column(name = "target_id")
  private Long targetId;

  // 알림 타입
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType notificationType;

  // 가변 데이터 저장 (예: 좋아요 10개, 50개 등 메타데이터 필요 시 사용)
  // JSON 형태의 문자열이나 단순 문자열로 저장하여 응답 시 조합
  @Column(name = "meta_data")
  private String metaData;

  // 읽음 여부 상태
  @Builder.Default
  @Column(nullable = false)
  private Boolean isRead = false;

  // 읽음 상태 업데이트
  public void markAsRead() {
    this.isRead = true;
  }
}
