package com.odos.odos_server_v2.config;

import java.security.Security;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WebPushProperties.class)
public class WebPushConfig {

  @Bean
  @ConditionalOnProperty(prefix = "notification.web-push", name = "enabled", havingValue = "true")
  public PushService pushService(WebPushProperties webPushProperties) throws Exception {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }

    return new PushService(
        webPushProperties.getPublicKey(),
        webPushProperties.getPrivateKey(),
        webPushProperties.getSubject());
  }
}
