package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long id;

  @Column(name = "receiver_id", nullable = false)
  private Long receiverId; // 알림을 받는 사용자 (Member)

  @Column(name = "sender_id")
  private Long senderId; // 알림을 발생시킨 사용자 (Member) - 시스템 알림일 경우 null

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private NotificationType type;

  @Column(name = "target_id")
  private Long targetId; // 관련된 엔티티(일지, 댓글, 챌린지 등)의 ID

  // 좋아요 마일스톤 달성 개수 같은 추가 정보를 담기 위해 확장 가능성을 열어둠 (Optional)
  @Column(name = "meta_data")
  private String metaData;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;

  @Builder
  public Notification(
      Long receiverId, Long senderId, NotificationType type, Long targetId, String metaData) {
    this.receiverId = receiverId;
    this.senderId = senderId;
    this.type = type;
    this.targetId = targetId;
    this.metaData = metaData;
    this.isRead = false;
  }

  public void markAsRead() {
    this.isRead = true;
  }
}
