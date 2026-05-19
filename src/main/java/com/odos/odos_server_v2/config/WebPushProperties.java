package com.odos.odos_server_v2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.web-push")
public class WebPushProperties {
  private Boolean enabled = false;
  private String publicKey;
  private String privateKey;
  private String subject;
}
