package com.odos.odos_server_v2.domain.notification.service;

import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationChannelType;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
import com.odos.odos_server_v2.domain.notification.repository.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FallbackAppPushSender implements NotificationSender {

  @Override
  public NotificationChannelType channel() {
    return NotificationChannelType.FCM;
  }

  @Override
  public void send(Notification notification, NotificationEndpoint endpoint) {
    log.info(
        "FCM sender not configured yet. notificationId={}, endpointId={}",
        notification.getId(),
        endpoint.getId());
  }
}
