package com.odos.odos_server_v2.domain.push.service;

import com.odos.odos_server_v2.domain.push.dto.DeviceTokenUpsertRequest;
import com.odos.odos_server_v2.domain.push.entity.DeviceToken;
import com.odos.odos_server_v2.domain.push.repository.DeviceTokenRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
  private final DeviceTokenRepository deviceTokenRepository;

  /** deviceId 기준 upsert. 같은 기기면 토큰/소유자만 갱신한다. */
  @Transactional
  public void upsert(Long memberId, DeviceTokenUpsertRequest request) {
    deviceTokenRepository
        .findByDeviceId(request.deviceId())
        .ifPresentOrElse(
            existing -> existing.refresh(memberId, request.token(), request.platform()),
            () ->
                deviceTokenRepository.save(
                    DeviceToken.builder()
                        .memberId(memberId)
                        .deviceId(request.deviceId())
                        .token(request.token())
                        .platform(request.platform())
                        .build()));
  }

  /** 로그아웃 시 해당 기기 토큰 비활성화. 소유자 불일치면 조용히 무시(남의 기기 삭제 방지). */
  @Transactional
  public void deactivate(Long memberId, String deviceId) {
    deviceTokenRepository
        .findByDeviceId(deviceId)
        .filter(token -> token.getMemberId().equals(memberId))
        .ifPresent(DeviceToken::deactivate);
  }

  /** FCM이 무효(UNREGISTERED 등) 판정한 토큰 정리. 발송 I/O 밖에서 호출한다. */
  @Transactional
  public void deactivateByIds(List<Long> tokenIds) {
    deviceTokenRepository.findAllById(tokenIds).forEach(DeviceToken::deactivate);
  }
}
