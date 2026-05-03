package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
import com.odos.odos_server_v2.domain.notification.enums.NotificationChannelType;

public interface NotificationSender {
  void send(Notification notification, NotificationEndpoint endpoint);

  boolean supports(NotificationEndpoint endpoint);

  NotificationChannelType channel();
}
