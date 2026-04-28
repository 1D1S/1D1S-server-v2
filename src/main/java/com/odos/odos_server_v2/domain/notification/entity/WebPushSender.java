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
    payload.put("body", notification.getMessage());
    payload.put("type", notification.getType().name());
    payload.put(
        "targetType",
        notification.getTargetType() != null ? notification.getTargetType().name() : null);
    payload.put("targetId", notification.getTargetId());
    payload.put("landingPath", notification.resolveLandingPath());

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

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
