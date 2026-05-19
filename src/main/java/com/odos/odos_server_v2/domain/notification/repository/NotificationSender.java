package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationChannelType;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;

public interface NotificationSender {
  void send(Notification notification, NotificationEndpoint endpoint);

  boolean supports(NotificationEndpoint endpoint);

  NotificationChannelType channel();
}
