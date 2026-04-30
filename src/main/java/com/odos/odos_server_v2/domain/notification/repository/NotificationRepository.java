package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // 수신자 기준으로 알림 목록 페이징 조회
  Page<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver, Pageable pageable);

  // 오래된 알림 삭제 (최근 30일 보존 기한 등 정책 적용 시 활용)
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
  void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

  // 수신자의 전체 알림 수 조회 (최대 100건 제한 등 검증 시 활용 가능)
  long countByReceiver(Member receiver);
}
