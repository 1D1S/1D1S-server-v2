package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {}
