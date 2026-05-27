// package com.odos.odos_server_v2.domain.notification.entity;
//
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.odos.odos_server_v2.config.WebPushProperties;
// import com.odos.odos_server_v2.domain.notification.entity.Notification;
// import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
// import java.nio.charset.Charset;
// import java.nio.charset.StandardCharsets;
// import java.util.Base64;
// import java.util.LinkedHashMap;
// import java.util.Map;
//
// import com.odos.odos_server_v2.domain.notification.repository.NotificationSender;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import nl.martijndwars.webpush.PushService;
// import nl.martijndwars.webpush.Subscription;
// import org.springframework.stereotype.Component;
//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class WebPushSender implements NotificationSender {
//
//    private static final Charset UTF8 = StandardCharsets.UTF_8;
//    private static final int SAFE_MAX_PAYLOAD_BYTES = 3900;
//
//    private final WebPushProperties webPushProperties;
//    private final PushService pushService;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void send(Notification notification, NotificationEndpoint endpoint) {
//        if (!Boolean.TRUE.equals(webPushProperties.getEnabled())) {
//            return;
//        }
//
//        if (!supports(endpoint)) {
//            log.warn("Web push endpoint is invalid. endpointId={}", endpoint.getId());
//            return;
//        }
//
//        try {
//            validateSubscription(endpoint);
//
//            Subscription subscription =
//                    new Subscription(
//                            endpoint.getEndpointUrl(),
//                            new Subscription.Keys(endpoint.getP256dh(),
// endpoint.getAuthSecret()));
//
//            nl.martijndwars.webpush.Notification pushNotification =
//                    new nl.martijndwars.webpush.Notification(subscription,
// toPayloadBytes(notification));
//
//            pushService.send(pushNotification);
//        } catch (IllegalArgumentException e) {
//            log.warn(
//                    "Invalid web push payload/subscription. notificationId={}, endpointId={},
// reason={}",
//                    notification.getId(),
//                    endpoint.getId(),
//                    e.getMessage());
//        } catch (Exception e) {
//            log.warn(
//                    "Failed to send web push. notificationId={}, endpointId={}",
//                    notification.getId(),
//                    endpoint.getId(),
//                    e);
//        }
//    }
//
//    @Override
//    public boolean supports(NotificationEndpoint endpoint) {
//        return endpoint != null
//                && Boolean.TRUE.equals(endpoint.getEnabled())
//                && hasText(endpoint.getEndpointUrl())
//                && hasText(endpoint.getP256dh())
//                && hasText(endpoint.getAuthSecret());
//    }
//
//    private byte[] toPayloadBytes(Notification notification) {
//        String payload = toPayload(notification);
//
//        if (!hasText(payload)) {
//            throw new IllegalArgumentException("Push payload is empty");
//        }
//
//        byte[] bytes = payload.getBytes(UTF8);
//
//        if (bytes.length > SAFE_MAX_PAYLOAD_BYTES) {
//            throw new IllegalArgumentException("Push payload too large: " + bytes.length);
//        }
//
//        return bytes;
//    }
//
//    private String toPayload(Notification notification) {
//        Map<String, Object> payload = new LinkedHashMap<>();
//        payload.put("notificationId", notification.getId());
//        payload.put("title", notification.resolvePushTitle());
//        payload.put("body", notification.getMessage());
//        payload.put("type", notification.getType().name());
//        payload.put("landingPath", notification.getLandingPath());
//
//        try {
//            return objectMapper.writeValueAsString(payload);
//        } catch (JsonProcessingException e) {
//            throw new IllegalArgumentException("Failed to serialize push payload", e);
//        }
//    }
//
//    private void validateSubscription(NotificationEndpoint endpoint) {
//        if (!hasText(endpoint.getEndpointUrl())) {
//            throw new IllegalArgumentException("endpointUrl is blank");
//        }
//
//        Base64.getUrlDecoder().decode(endpoint.getP256dh());
//        Base64.getUrlDecoder().decode(endpoint.getAuthSecret());
//    }
//
//    private boolean hasText(String value) {
//        return value != null && !value.isBlank();
//    }
// }

package com.odos.odos_server_v2.domain.notification.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odos.odos_server_v2.config.WebPushProperties;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationChannelType;
import com.odos.odos_server_v2.domain.notification.repository.NotificationSender;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebPushSender implements NotificationSender {

  private static final int SAFE_MAX_PAYLOAD_BYTES = 3900;

  private final WebPushProperties webPushProperties;
  private final PushService pushService;
  private final ObjectMapper objectMapper;

  @Override
  public void send(Notification notification, NotificationEndpoint endpoint) {
    if (!Boolean.TRUE.equals(webPushProperties.getEnabled())) {
      return;
    }

    if (!supports(endpoint)) {
      log.warn("Web push endpoint is invalid. endpointId={}", endpoint.getId());
      return;
    }

    try {
      validateSubscription(endpoint);

      Subscription subscription =
          new Subscription(
              endpoint.getEndpointUrl(),
              new Subscription.Keys(endpoint.getP256dh(), endpoint.getAuthSecret()));

      String payload = toPayload(notification);

      nl.martijndwars.webpush.Notification pushNotification =
          new nl.martijndwars.webpush.Notification(subscription, payload);

      pushService.send(pushNotification);
    } catch (IllegalArgumentException e) {
      log.warn(
          "Invalid web push payload/subscription. notificationId={}, endpointId={}, reason={}",
          notification.getId(),
          endpoint.getId(),
          e.getMessage());
    } catch (Exception e) {
      log.warn(
          "Failed to send web push. notificationId={}, endpointId={}",
          notification.getId(),
          endpoint.getId(),
          e);
    }
  }

  @Override
  public boolean supports(NotificationEndpoint endpoint) {
    return endpoint != null
        && Boolean.TRUE.equals(endpoint.getEnabled())
        && hasText(endpoint.getEndpointUrl())
        && hasText(endpoint.getP256dh())
        && hasText(endpoint.getAuthSecret());
  }

  private String toPayload(Notification notification) {
    String payload = toPayloadJson(notification);
    if (!hasText(payload)) {
      throw new IllegalArgumentException("Push payload is empty");
    }

    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
    if (bytes.length > SAFE_MAX_PAYLOAD_BYTES) {
      throw new IllegalArgumentException("Push payload too large: " + bytes.length);
    }

    return payload;
  }

  private String toPayloadJson(Notification notification) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("notificationId", notification.getId());
    payload.put("title", notification.resolvePushTitle());
    payload.put("body", generateMessage(notification));
    payload.put("type", notification.getType().name());
    payload.put(
        "targetType",
        notification.getTargetType() != null ? notification.getTargetType().name() : null);
    payload.put("targetId", notification.getTargetId());

    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to serialize push payload", e);
    }
  }

  private void validateSubscription(NotificationEndpoint endpoint) {
    if (!hasText(endpoint.getEndpointUrl())) {
      throw new IllegalArgumentException("endpointUrl is blank");
    }

    Base64.getUrlDecoder().decode(endpoint.getP256dh());
    Base64.getUrlDecoder().decode(endpoint.getAuthSecret());
  }

  @Override
  public NotificationChannelType channel() {
    return NotificationChannelType.WEB_PUSH;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String generateMessage(Notification notification) {
    String actorNickname =
        notification.getResolvedActor() != null
            ? notification.getResolvedActor().getNickname()
            : null;
    Integer groupedCount = notification.getResolvedGroupedCount();
    String relatedContent = notification.getResolvedRelatedContent();

    return switch (notification.getResolvedType()) {
      case FRIEND_REQUEST -> String.format("%s님이 친구 신청을 보냈어요.", actorNickname);
      case FRIEND_ACCEPT -> String.format("%s님이 친구 신청을 수락했습니다. 이제 일지를 확인해보세요!", actorNickname);
      case FRIEND_DIARY_CREATED ->
          String.format("%s님이 일지를 등록했어요: %s", actorNickname, relatedContent);
      case MY_DIARY_COMMENTED -> {
        if (groupedCount != null && groupedCount > 1) {
          yield String.format("%s님 외 %d명이 댓글을 달았습니다.", actorNickname, groupedCount - 1);
        }
        yield String.format("%s님이 댓글을 달았습니다: %s", actorNickname, relatedContent);
      }
      case MY_COMMENT_REPLIED ->
          String.format("%s님이 내 댓글에 답글을 남겼습니다: %s", actorNickname, relatedContent);
      case DIARY_LIKE_MILESTONE -> {
        if ("1".equals(relatedContent)) {
          yield "작성하신 일지가 좋아요를 받았어요! 🎉";
        }
        yield String.format("작성하신 일지의 좋아요가 %s개를 넘어갔어요! 🎉", relatedContent);
      }
      case CHALLENGE_APPLIED ->
          String.format("%s님이 %s 챌린지에 참여 신청했습니다.", actorNickname, relatedContent);
      case CHALLENGE_APPROVED -> String.format("%s 챌린지원이 되었습니다! 열심히 참여해봐요!", relatedContent);
      case CHALLENGE_REJECTED -> String.format("%s 챌린지 참여가 거절되었습니다.", relatedContent);
    };
  }
}
