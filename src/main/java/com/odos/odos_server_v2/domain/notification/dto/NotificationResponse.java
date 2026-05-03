package com.odos.odos_server_v2.domain.notification.dto;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.enums.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.enums.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.enums.NotificationType;
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
        .message(notification.getResolvedMessage())
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
