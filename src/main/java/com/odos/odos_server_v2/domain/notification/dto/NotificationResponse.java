package com.odos.odos_server_v2.domain.notification.dto;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationType;
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

  public static String formatMessage(
      NotificationType type, String actorNickname, String dynamicArgs, Integer groupedCount) {
    return switch (type) {
      case FRIEND_REQUEST -> String.format("%s님이 친구 신청을 보냈어요.", actorNickname);
      case FRIEND_ACCEPT -> String.format("%s님이 친구 신청을 수락했습니다. 이제 일지를 확인해보세요!", actorNickname);
      case FRIEND_DIARY_CREATED -> String.format("%s님이 일지를 등록했어요: %s", actorNickname, dynamicArgs);
      case MY_DIARY_COMMENTED -> {
        if (groupedCount != null && groupedCount > 1) {
          yield String.format("%s님 외 %d명이 댓글을 달았습니다.", actorNickname, groupedCount - 1);
        }
        yield String.format("%s님이 댓글을 달았습니다: %s", actorNickname, dynamicArgs);
      }
      case MY_COMMENT_REPLIED ->
          String.format("%s님이 내 댓글에 답글을 남겼습니다: %s", actorNickname, dynamicArgs);
      case DIARY_LIKE_MILESTONE -> {
        int milestone = dynamicArgs != null ? Integer.parseInt(dynamicArgs) : 1;
        yield milestone == 1
            ? "작성하신 일지가 좋아요를 받았어요! 🎉"
            : String.format("작성하신 일지의 좋아요가 %d개를 넘어갔어요! 🎉", milestone);
      }
      case CHALLENGE_APPROVED -> String.format("%s 챌린지원이 되었습니다! 열심히 참여해봐요!", dynamicArgs);
      case CHALLENGE_REJECTED -> String.format("%s 챌린지 참여가 거절되었습니다.", dynamicArgs);
    };
  }

  public static NotificationResponse from(Notification notification, String actorProfileUrl) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .category(notification.getResolvedCategory())
        .type(notification.getResolvedType())
        .message(
            formatMessage(
                notification.getResolvedType(),
                notification.getResolvedActor() != null
                    ? notification.getResolvedActor().getNickname()
                    : null,
                notification.getResolvedDynamicArgs(),
                notification.getResolvedGroupedCount()))
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
}
