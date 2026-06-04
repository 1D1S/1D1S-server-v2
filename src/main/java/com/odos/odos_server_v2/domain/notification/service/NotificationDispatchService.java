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
    long startedAt = System.currentTimeMillis();
    Member receiver = notification.getReceiver();
    List<NotificationEndpoint> endpoints = endpointRepository.findByMemberAndIsActiveTrue(receiver);
    log.info(
        "Notification push send started. notificationId={}, endpointCount={}, elapsedMs=0",
        notification.getId(),
        endpoints.size());

    for (NotificationEndpoint endpoint : endpoints) {
      NotificationSender sender = findSender(endpoint);
      if (sender == null) {
        continue;
      }

      long endpointStartedAt = System.currentTimeMillis();
      try {
        sender.send(notification, endpoint);
        log.info(
            "Notification push send completed. notificationId={}, endpointId={}, channel={}, elapsedMs={}",
            notification.getId(),
            endpoint.getId(),
            sender.channel(),
            elapsedMs(endpointStartedAt));
      } catch (Exception e) {
        log.warn(
            "Notification push send failed. notificationId={}, endpointId={}, channel={}, elapsedMs={}",
            notification.getId(),
            endpoint.getId(),
            sender.channel(),
            elapsedMs(endpointStartedAt),
            e);
      }
    }

    log.info(
        "Notification push send finished. notificationId={}, endpointCount={}, elapsedMs={}",
        notification.getId(),
        endpoints.size(),
        elapsedMs(startedAt));
  }

  private NotificationSender findSender(NotificationEndpoint endpoint) {
    for (NotificationSender sender : senders) {
      if (sender.supports(endpoint)) {
        return sender;
      }
    }
    return null;
  }

  private long elapsedMs(long startedAt) {
    return System.currentTimeMillis() - startedAt;
  }
}
