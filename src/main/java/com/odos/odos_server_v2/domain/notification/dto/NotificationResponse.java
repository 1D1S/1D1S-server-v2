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
    return NotificationResponse.builder()
        .id(notification.getId())
        .category(notification.getResolvedCategory())
        .type(notification.getResolvedType())
        .message(notification.buildMessage())
        .targetType(notification.getResolvedTargetType())
        .targetId(notification.getResolvedTargetId())
        .isRead(notification.getIsRead())
        .groupedCount(notification.getResolvedGroupedCount())
        .actorId(
            notification.getResolvedActor() == null
                ? null
                : notification.getResolvedActor().getId())
        .actorNickname(
            notification.getResolvedActor() == null
                ? null
                : notification.getResolvedActor().getNickname())
        .actorProfileUrl(actorProfileUrl)
        .createdAt(notification.getCreatedAt())
        .build();
  }

  private static String buildMessage(Notification notification) {
    NotificationType type = notification.getResolvedType();
    String targetInfo = notification.getResolvedTargetInfo();
    String actorNickname =
        notification.getResolvedActor() != null
            ? notification.getResolvedActor().getNickname()
            : "알 수 없는 사용자";
    Integer groupedCount = notification.getResolvedGroupedCount();

    return switch (type) {
      case FRIEND_REQUEST -> String.format("%s님이 친구 신청을 보냈어요.", actorNickname);
      case FRIEND_ACCEPT -> String.format("%s님이 친구 신청을 수락했습니다. 이제 일지를 확인해보세요!", actorNickname);
      case FRIEND_DIARY_CREATED -> String.format("%s님이 일지를 등록했어요: %s", actorNickname, targetInfo);
      case MY_DIARY_COMMENTED -> {
        if (groupedCount != null && groupedCount > 1) {
          yield String.format("%s님 외 %d명이 댓글을 달았습니다.", actorNickname, groupedCount - 1);
        }
        yield String.format("%s님이 댓글을 달았습니다: %s", actorNickname, targetInfo);
      }
      case MY_COMMENT_REPLIED ->
          String.format("%s님이 내 댓글에 답글을 남겼습니다: %s", actorNickname, targetInfo);
      case DIARY_LIKE_MILESTONE -> {
        if ("1".equals(targetInfo)) {
          yield "작성하신 일지가 좋아요를 받았어요! 🎉";
        }
        yield String.format("작성하신 일지의 좋아요가 %s개를 넘어갔어요! 🎉", targetInfo);
      }
      case CHALLENGE_APPROVED -> String.format("%s 챌린지원이 되었습니다! 열심히 참여해봐요!", targetInfo);
      case CHALLENGE_REJECTED -> String.format("%s 챌린지 참여가 거절되었습니다.", targetInfo);
    };
  }
}
