package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long id;

  @Column(nullable = false)
  private Long receiverId; // Member ID who receives the notification

  @Column
  private Long senderId; // Member ID who triggered the event (can be null for system notifications)

  @Column private Long targetId; // Reference ID for related domain (e.g., DiaryId, ChallengeId)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationCategory category;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  @Column
  private Integer milestoneCount; // Only used for LIKE_MILESTONE, represents the number of likes

  public void markAsRead() {
    this.isRead = true;
  }
}
