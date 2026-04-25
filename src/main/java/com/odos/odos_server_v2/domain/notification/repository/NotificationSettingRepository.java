package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.NotificationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

  // 회원 ID로 알림 설정 조회
  Optional<NotificationSetting> findByMemberId(Long memberId);
}
