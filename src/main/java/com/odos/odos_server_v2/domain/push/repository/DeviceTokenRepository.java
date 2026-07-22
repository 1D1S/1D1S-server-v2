package com.odos.odos_server_v2.domain.push.repository;

import com.odos.odos_server_v2.domain.push.entity.DeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
  Optional<DeviceToken> findByDeviceId(String deviceId);

  List<DeviceToken> findByMemberIdAndIsActiveTrue(Long memberId);
}
