package com.odos.odos_server_v2.domain.push.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.odos.odos_server_v2.domain.push.dto.PushMessage;
import com.odos.odos_server_v2.domain.push.entity.DeviceToken;
import com.odos.odos_server_v2.domain.push.repository.DeviceTokenRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 회원의 활성 단말 토큰으로 FCM(HTTP v1, Admin SDK) 푸시를 발송한다. Firebase 미설정 시 조용히 no-op. 무효 토큰(UNREGISTERED 등)은
 * 발송 후 비활성화한다.
 *
 * <p>실제 발송 트리거(채팅/공지 등)는 이 서비스의 {@link #sendToMember}를 호출하는 지점만 추가하면 된다.
 */
@Slf4j
@Service
public class PushSender {
  private final ObjectProvider<FirebaseMessaging> firebaseMessaging;
  private final DeviceTokenRepository deviceTokenRepository;
  private final DeviceTokenService deviceTokenService;

  public PushSender(
      ObjectProvider<FirebaseMessaging> firebaseMessaging,
      DeviceTokenRepository deviceTokenRepository,
      DeviceTokenService deviceTokenService) {
    this.firebaseMessaging = firebaseMessaging;
    this.deviceTokenRepository = deviceTokenRepository;
    this.deviceTokenService = deviceTokenService;
  }

  public void sendToMember(Long memberId, PushMessage message) {
    FirebaseMessaging messaging = firebaseMessaging.getIfAvailable();
    if (messaging == null) {
      log.debug("FCM 미설정으로 발송 건너뜀. memberId={}", memberId);
      return;
    }

    List<DeviceToken> devices = deviceTokenRepository.findByMemberIdAndIsActiveTrue(memberId);
    if (devices.isEmpty()) {
      return;
    }

    List<String> tokens = devices.stream().map(DeviceToken::getToken).toList();
    MulticastMessage multicast =
        MulticastMessage.builder()
            .setNotification(
                Notification.builder().setTitle(message.title()).setBody(message.body()).build())
            .putAllData(message.data())
            .addAllTokens(tokens)
            .build();

    BatchResponse response;
    try {
      response = messaging.sendEachForMulticast(multicast);
    } catch (Exception e) {
      log.error("FCM 발송 실패. memberId={}", memberId, e);
      return;
    }

    List<Long> invalidTokenIds = new ArrayList<>();
    List<SendResponse> responses = response.getResponses();
    for (int i = 0; i < responses.size(); i++) {
      SendResponse sr = responses.get(i);
      if (sr.isSuccessful() || sr.getException() == null) {
        continue;
      }
      MessagingErrorCode code = sr.getException().getMessagingErrorCode();
      if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
        invalidTokenIds.add(devices.get(i).getId());
      }
    }
    if (!invalidTokenIds.isEmpty()) {
      deviceTokenService.deactivateByIds(invalidTokenIds);
      log.info("FCM 무효 토큰 {}건 비활성화. memberId={}", invalidTokenIds.size(), memberId);
    }
  }
}
