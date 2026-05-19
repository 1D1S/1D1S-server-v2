package com.odos.odos_server_v2.domain.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationEndpointDeleteRequest {
  private String endpointUrl;
}
