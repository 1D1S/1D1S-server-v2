package com.odos.odos_server_v2.domain.notification.dto;

import com.odos.odos_server_v2.domain.notification.entity.NotificationEndpoint;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationEndpointResponse {
  private Long id;
  private String endpointUrl;
  private Boolean isActive;
  private LocalDateTime lastSeenAt;

  public static NotificationEndpointResponse from(NotificationEndpoint endpoint) {
    return NotificationEndpointResponse.builder()
        .id(endpoint.getId())
        .endpointUrl(endpoint.getEndpointUrl())
        .isActive(endpoint.getIsActive())
        .lastSeenAt(endpoint.getLastSeenAt())
        .build();
  }
}
