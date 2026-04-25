package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // 특정 사용자의 알림을 최신순으로 조회
  List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

  // 읽지 않은 알림이 있는지 확인
  boolean existsByReceiverIdAndIsReadFalse(Long receiverId);

  // 특정 사용자의 전체 알림 개수 조회 (100건 초과 여부 확인용 등)
  long countByReceiverId(Long receiverId);

  // 오래된 알림을 삭제하기 위한 쿼리 (30일이 지났거나, 특정 개수를 초과한 경우 스케줄러에서 활용)
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :thresholdDate")
  void deleteByCreatedAtBefore(@Param("thresholdDate") LocalDateTime thresholdDate);

  // 100건 초과분 삭제를 위해 삭제할 ID 리스트를 받아와 삭제
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.id IN :ids")
  void deleteAllByIdIn(@Param("ids") List<Long> ids);

  // 삭제 대상 ID 목록을 조회 (Native Query 또는 Pageable을 활용하여 구현 가능)
  // Spring Data JPA에서 limit 처리를 위해 Pageable을 사용할 수 있습니다.
  @Query(
      "SELECT n.id FROM Notification n WHERE n.receiverId = :receiverId ORDER BY n.createdAt ASC")
  List<Long> findOldestNotificationIdsByReceiverId(
      @Param("receiverId") Long receiverId, Pageable pageable);
}
