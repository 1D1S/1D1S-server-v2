package com.odos.odos_server_v2.domain.notification.entity;

import static com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationCategory.*;
import static java.awt.Event.HOME;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
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
    name = "notification",
    indexes = {
      @Index(
          name = "idx_notification_receiver_created_at",
          columnList = "receiver_member_id, created_at"),
      @Index(name = "idx_notification_expires_at", columnList = "expires_at"),
      @Index(name = "idx_notification_receiver_is_read", columnList = "receiver_member_id, is_read")
    })
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_member_id", nullable = false)
  private Member receiver;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "notification_event_id")
  private NotificationEvent event;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_member_id")
  private Member actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NotificationType type;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private NotificationTargetType targetType;

  @Column(name = "target_id")
  private Long targetId;

  @Builder.Default
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Column(name = "grouped_count")
  private Integer groupedCount;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(length = 255)
  private String landingPath;

  public void markAsRead() {
    this.isRead = true;
    this.readAt = LocalDateTime.now();
  }

  public Member getResolvedActor() {
    return event != null ? event.getActor() : actor;
  }

  public NotificationCategory getResolvedCategory() {
    return event != null ? event.getCategory() : category;
  }

  public NotificationType getResolvedType() {
    return event != null ? event.getType() : type;
  }

  public String getResolvedMessage() {
    return event != null ? event.getMessage() : message;
  }

  public NotificationTargetType getResolvedTargetType() {
    return event != null ? event.getTargetType() : targetType;
  }

  public Long getResolvedTargetId() {
    return event != null ? event.getTargetId() : targetId;
  }

  public Integer getResolvedGroupedCount() {
    return event != null ? event.getGroupedCount() : groupedCount;
  }

  public boolean isExpired(LocalDateTime now) {
    return this.expiresAt.isBefore(now);
  }

  public String resolvePushTitle() {
    return switch (type) {
      case FRIEND_REQUEST, FRIEND_ACCEPT -> "친구 알림";
      case FRIEND_DIARY_CREATED, MY_DIARY_COMMENTED, MY_COMMENT_REPLIED, DIARY_LIKE_MILESTONE ->
          "일지 알림";
      case CHALLENGE_APPROVED, CHALLENGE_REJECTED -> "챌린지 알림";
    };
  }

  public String resolveLandingPath() {
    if (targetType == null) {
      return "/home";
    }

    return switch (targetType) {
      case MEMBER_PROFILE -> targetId != null ? "/members/" + targetId : "/home";
      case DIARY -> targetId != null ? "/diaries/" + targetId : "/home";
      case COMMENT -> targetId != null ? "/comments/" + targetId : "/home";
      case CHALLENGE -> targetId != null ? "/challenges/" + targetId : "/home";
      case CHALLENGE_LIST -> "/challenges";
      case HOME -> "/home";
    };
  }
}
