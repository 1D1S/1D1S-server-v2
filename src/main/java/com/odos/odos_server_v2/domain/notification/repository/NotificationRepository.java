package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
  void deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

  // Example native query for keeping only the latest 100 notifications per user
  // (Actual logic might be implemented in a service or via a scheduled job, but adding a base query
  // structure just in case)
  @Modifying
  @Query(
      value =
          "DELETE FROM notification WHERE notification_id NOT IN ("
              + "  SELECT notification_id FROM ("
              + "    SELECT notification_id FROM notification WHERE receiver_id = :receiverId ORDER BY created_at DESC LIMIT :limit"
              + "  ) AS temp"
              + ") AND receiver_id = :receiverId",
      nativeQuery = true)
  void deleteNotificationsExceedingLimit(
      @Param("receiverId") Long receiverId, @Param("limit") int limit);
}
