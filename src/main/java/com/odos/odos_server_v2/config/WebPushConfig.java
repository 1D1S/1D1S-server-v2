package com.odos.odos_server_v2.config;

import nl.martijndwars.webpush.PushService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WebPushProperties.class)
public class WebPushConfig {

  @Bean
  public PushService pushService(WebPushProperties webPushProperties) {
    return new PushService(
        webPushProperties.getPublicKey(),
        webPushProperties.getPrivateKey(),
        webPushProperties.getSubject());
  }
}
