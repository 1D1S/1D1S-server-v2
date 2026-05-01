package com.odos.odos_server_v2.domain.notification.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
import com.odos.odos_server_v2.domain.notification.repository.NotificationEndpointRepository;
import com.odos.odos_server_v2.domain.notification.repository.NotificationSender;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationDispatchService {

  private final NotificationEndpointRepository endpointRepository;
  private final List<NotificationSender> senders;

  public NotificationDispatchService(
      NotificationEndpointRepository endpointRepository, List<NotificationSender> senders) {
    this.endpointRepository = endpointRepository;
    this.senders = senders;
  }

  public void dispatch(Notification notification) {
    Member receiver = notification.getReceiver();
    List<NotificationEndpoint> endpoints = endpointRepository.findByMemberAndIsActiveTrue(receiver);

    for (NotificationEndpoint endpoint : endpoints) {
      NotificationSender sender = findSender(endpoint);
      if (sender == null) {
        continue;
      }

      try {
        sender.send(notification, endpoint);
      } catch (Exception e) {
        log.warn(
            "Notification channel send failed. notificationId={}, endpointId={}",
            notification.getId(),
            endpoint.getId(),
            e);
      }
    }
  }

  private NotificationSender findSender(NotificationEndpoint endpoint) {
    for (NotificationSender sender : senders) {
      if (sender.supports(endpoint)) {
        return sender;
      }
    }
    return null;
  }
}
