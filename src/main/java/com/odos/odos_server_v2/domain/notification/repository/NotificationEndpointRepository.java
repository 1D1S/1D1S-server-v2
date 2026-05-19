package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEndpointRepository extends JpaRepository<NotificationEndpoint, Long> {

  List<NotificationEndpoint> findByMemberAndIsActiveTrue(Member member);

  Optional<NotificationEndpoint> findByMemberIdAndEndpointUrl(Long memberId, String endpointUrl);
}
