package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver, Pageable pageable);

  Page<Notification> findByReceiverOrderByCreatedAtAsc(Member receiver, Pageable pageable);

  long countByReceiverAndIsReadFalse(Member receiver);

  long countByReceiver(Member receiver);

  void deleteByExpiresAtBefore(LocalDateTime now);

  @Modifying
  @Query(
      "update Notification n set n.isRead = true, n.readAt = :readAt where n.receiver = :receiver and n.isRead = false")
  int markAllAsRead(@Param("receiver") Member receiver, @Param("readAt") LocalDateTime readAt);

  Optional<Notification>
      findFirstByReceiverAndTypeAndTargetIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
          Member receiver, NotificationType type, Long targetId, LocalDateTime from);
}
