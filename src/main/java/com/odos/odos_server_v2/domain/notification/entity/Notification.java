package com.odos.odos_server_v2.domain.notification.entity;

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
@NoArgsConstructor
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

  @Column(name = "target_info", columnDefinition = "TEXT")
  private String targetInfo;

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

  public String getResolvedTargetInfo() {
    return event != null ? event.getTargetInfo() : targetInfo;
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

  public void updateGroupedTargetInfo(String targetInfo, Integer groupedCount) {
    this.targetInfo = targetInfo;
    this.groupedCount = groupedCount;
    if (this.event != null) {
      this.event.updateGroupedTargetInfo(targetInfo, groupedCount);
    }
  }

  public String buildMessage() {
    NotificationType currentType = getResolvedType();
    String currentTargetInfo = getResolvedTargetInfo();
    String actorNickname =
        getResolvedActor() != null ? getResolvedActor().getNickname() : "알 수 없는 사용자";
    Integer currentGroupedCount = getResolvedGroupedCount();

    return switch (currentType) {
      case FRIEND_REQUEST -> String.format("%s님이 친구 신청을 보냈어요.", actorNickname);
      case FRIEND_ACCEPT -> String.format("%s님이 친구 신청을 수락했습니다. 이제 일지를 확인해보세요!", actorNickname);
      case FRIEND_DIARY_CREATED ->
          String.format("%s님이 일지를 등록했어요: %s", actorNickname, currentTargetInfo);
      case MY_DIARY_COMMENTED -> {
        if (currentGroupedCount != null && currentGroupedCount > 1) {
          yield String.format("%s님 외 %d명이 댓글을 달았습니다.", actorNickname, currentGroupedCount - 1);
        }
        yield String.format("%s님이 댓글을 달았습니다: %s", actorNickname, currentTargetInfo);
      }
      case MY_COMMENT_REPLIED ->
          String.format("%s님이 내 댓글에 답글을 남겼습니다: %s", actorNickname, currentTargetInfo);
      case DIARY_LIKE_MILESTONE -> {
        if ("1".equals(currentTargetInfo)) {
          yield "작성하신 일지가 좋아요를 받았어요! 🎉";
        }
        yield String.format("작성하신 일지의 좋아요가 %s개를 넘어갔어요! 🎉", currentTargetInfo);
      }
      case CHALLENGE_APPROVED -> String.format("%s 챌린지원이 되었습니다! 열심히 참여해봐요!", currentTargetInfo);
      case CHALLENGE_REJECTED -> String.format("%s 챌린지 참여가 거절되었습니다.", currentTargetInfo);
    };
  }

  public String resolvePushTitle() {
    return switch (type) {
      case FRIEND_REQUEST, FRIEND_ACCEPT -> "친구 알림";
      case FRIEND_DIARY_CREATED, MY_DIARY_COMMENTED, MY_COMMENT_REPLIED, DIARY_LIKE_MILESTONE ->
          "일지 알림";
      case CHALLENGE_APPROVED, CHALLENGE_REJECTED -> "챌린지 알림";
    };
  }
}
