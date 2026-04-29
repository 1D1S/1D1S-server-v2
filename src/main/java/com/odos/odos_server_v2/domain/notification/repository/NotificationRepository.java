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
  @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
  void deleteOlderThan(@Param("date") LocalDateTime date);
}
