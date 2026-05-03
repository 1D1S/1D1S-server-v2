package com.odos.odos_server_v2.domain.notification.service;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
import com.odos.odos_server_v2.domain.notification.enums.NotificationChannelType;
import com.odos.odos_server_v2.domain.notification.repository.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FallbackApnsSender implements NotificationSender {

  @Override
  public NotificationChannelType channel() {
    return NotificationChannelType.APNS;
  }

  @Override
  public void send(Notification notification, NotificationEndpoint endpoint) {
    log.info(
        "APNS sender not configured yet. notificationId={}, endpointId={}",
        notification.getId(),
        endpoint.getId());
  }

  @Override
  public boolean supports(NotificationEndpoint endpoint) {
    return false;
  }
}
