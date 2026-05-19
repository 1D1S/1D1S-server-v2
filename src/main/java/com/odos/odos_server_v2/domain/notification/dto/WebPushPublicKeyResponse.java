package com.odos.odos_server_v2.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WebPushPublicKeyResponse {
  private String publicKey;
}
