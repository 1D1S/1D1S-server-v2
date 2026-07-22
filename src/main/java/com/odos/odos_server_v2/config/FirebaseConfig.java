package com.odos.odos_server_v2.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Firebase Admin SDK 초기화. {@code FIREBASE_SERVICE_ACCOUNT_JSON}에는 서비스계정 JSON의 <b>내용</b> 또는 <b>파일
 * 경로</b>를 넣는다. 값이 비어 있으면 빈을 만들지 않고(=FCM 발송 비활성) 부팅은 정상 진행한다.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

  @Bean
  public FirebaseMessaging firebaseMessaging(
      @Value("${firebase.service-account-json:}") String serviceAccount) {
    if (serviceAccount == null || serviceAccount.isBlank()) {
      log.warn("FIREBASE_SERVICE_ACCOUNT_JSON 미설정 → FCM 푸시 발송 비활성화(부팅은 정상).");
      return null;
    }
    try (InputStream credentials = openCredentials(serviceAccount.trim())) {
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(credentials))
              .build();
      FirebaseApp app =
          FirebaseApp.getApps().isEmpty()
              ? FirebaseApp.initializeApp(options)
              : FirebaseApp.getInstance();
      log.info("Firebase Admin SDK 초기화 완료. FCM 발송 활성화.");
      return FirebaseMessaging.getInstance(app);
    } catch (Exception e) {
      log.error("Firebase 초기화 실패 → FCM 발송 비활성화. 설정값을 확인하세요.", e);
      return null;
    }
  }

  private InputStream openCredentials(String value) throws Exception {
    if (value.startsWith("{")) {
      return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
    return new FileInputStream(value);
  }
}
