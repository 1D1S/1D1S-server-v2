package com.odos.odos_server_v2.domain.notification.service;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import com.odos.odos_server_v2.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncNotificationPushService {

  private final NotificationRepository notificationRepository;
  private final NotificationDispatchService notificationDispatchService;

  @Async("notificationTaskExecutor")
  @Transactional(readOnly = true)
  public void dispatchAsync(Long notificationId) {
    long startedAt = System.currentTimeMillis();
    log.info("Notification push dispatch started. notificationId={}, elapsedMs=0", notificationId);

    try {
      Notification notification =
          notificationRepository
              .findById(notificationId)
              .orElseThrow(
                  () -> new IllegalArgumentException("Notification not found: " + notificationId));

      notificationDispatchService.dispatch(notification);

      log.info(
          "Notification push dispatch completed. notificationId={}, elapsedMs={}",
          notificationId,
          elapsedMs(startedAt));
    } catch (Exception e) {
      log.warn(
          "Notification push dispatch failed. notificationId={}, elapsedMs={}",
          notificationId,
          elapsedMs(startedAt),
          e);
    }
  }

  private long elapsedMs(long startedAt) {
    return System.currentTimeMillis() - startedAt;
  }
}
