package com.odos.odos_server_v2.domain.notification.dto;

import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

  private Long id;
  private NotificationCategory category;
  private NotificationType type;
  private String message;
  private NotificationTargetType targetType;
  private Long targetId;
  private Boolean isRead;
  private Integer groupedCount;
  private Long actorId;
  private String actorNickname;
  private String actorProfileUrl;
  private LocalDateTime createdAt;

  public static NotificationResponse from(Notification notification, String actorProfileUrl) {
    String actorNickname =
        notification.getResolvedActor() == null
            ? null
            : notification.getResolvedActor().getNickname();
    String message =
        generateMessage(
            notification.getResolvedType(),
            actorNickname,
            notification.getResolvedRelatedContent(),
            notification.getResolvedGroupedCount());

    return NotificationResponse.builder()
        .id(notification.getId())
        .category(notification.getResolvedCategory())
        .type(notification.getResolvedType())
        .message(message)
        .targetType(notification.getResolvedTargetType())
        .targetId(notification.getResolvedTargetId())
        .isRead(notification.getIsRead())
        .groupedCount(notification.getResolvedGroupedCount())
        .actorId(
            notification.getResolvedActor() == null
                ? null
                : notification.getResolvedActor().getId())
        .actorNickname(actorNickname)
        .actorProfileUrl(actorProfileUrl)
        .createdAt(notification.getCreatedAt())
        .build();
  }

  private static String generateMessage(
      NotificationType type, String actorNickname, String relatedContent, Integer groupedCount) {
    switch (type) {
      case FRIEND_REQUEST:
        return String.format("%s님이 친구 신청을 보냈어요.", actorNickname);
      case FRIEND_ACCEPT:
        return String.format("%s님이 친구 신청을 수락했습니다. 이제 일지를 확인해보세요!", actorNickname);
      case FRIEND_DIARY_CREATED:
        return String.format("%s님이 일지를 등록했어요: %s", actorNickname, relatedContent);
      case MY_DIARY_COMMENTED:
        if (groupedCount != null && groupedCount > 1) {
          // If relatedContent contains the first actor's nickname.
          String firstActor = relatedContent != null ? relatedContent : actorNickname;
          return String.format("%s님 외 %d명이 댓글을 달았습니다.", firstActor, groupedCount - 1);
        }
        return String.format("%s님이 댓글을 달았습니다: %s", actorNickname, relatedContent);
      case MY_COMMENT_REPLIED:
        return String.format("%s님이 내 댓글에 답글을 남겼습니다: %s", actorNickname, relatedContent);
      case DIARY_LIKE_MILESTONE:
        if (groupedCount != null && groupedCount == 1) {
          return "작성하신 일지가 좋아요를 받았어요! 🎉";
        }
        return String.format(
            "작성하신 일지의 좋아요가 %d개를 넘어갔어요! 🎉", groupedCount != null ? groupedCount : 0);
      case CHALLENGE_APPLIED:
        return String.format("%s님이 %s 챌린지에 참여 신청했습니다.", actorNickname, relatedContent);
      case CHALLENGE_APPROVED:
        return String.format("%s 챌린지원이 되었습니다! 열심히 참여해봐요!", relatedContent);
      case CHALLENGE_REJECTED:
        return String.format("%s 챌린지 참여가 거절되었습니다.", relatedContent);
      default:
        return "";
    }
  }
}
