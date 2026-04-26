package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

  long countByReceiverId(Long receiverId);

  // Get notifications ordered by creation date ascending (oldest first)
  @Query("SELECT n FROM Notification n WHERE n.receiverId = :receiverId ORDER BY n.createdAt ASC")
  List<Notification> findOldestByReceiverId(
      @Param("receiverId") Long receiverId, Pageable pageable);

  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
  void deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
