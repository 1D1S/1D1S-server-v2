package com.odos.odos_server_v2.domain.notification.event;

import com.odos.odos_server_v2.domain.notification.service.AsyncNotificationPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationPushEventListener {

  private final AsyncNotificationPushService asyncNotificationPushService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(NotificationPushRequestedEvent event) {
    asyncNotificationPushService.dispatchAsync(event.notificationId());
  }
}
