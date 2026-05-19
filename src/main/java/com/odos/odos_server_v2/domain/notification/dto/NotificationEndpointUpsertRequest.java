package com.odos.odos_server_v2.domain.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationEndpointUpsertRequest {
  private String endpointUrl;
  private String p256dh;
  private String authSecret;
}
