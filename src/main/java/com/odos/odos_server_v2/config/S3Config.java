package com.odos.odos_server_v2.config;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {
  @Bean
  public S3Client s3Client() {
    // 타임아웃 미설정 시 느린/멈춘 S3 호출이 무한정 지연될 수 있고, 업로드가 @Transactional 안에서
    // 실행되면 그 시간만큼 DB 커넥션을 점유해 커넥션 풀이 고갈될 수 있다. 전체/시도별 상한을 둔다.
    return S3Client.builder()
        .region(Region.AP_NORTHEAST_2)
        .overrideConfiguration(
            o ->
                o.apiCallTimeout(Duration.ofSeconds(30))
                    .apiCallAttemptTimeout(Duration.ofSeconds(15)))
        .build();
  }

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder().region(Region.AP_NORTHEAST_2).build();
  }
}
