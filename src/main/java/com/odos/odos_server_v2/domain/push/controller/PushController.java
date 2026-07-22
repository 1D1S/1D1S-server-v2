package com.odos.odos_server_v2.domain.push.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.push.dto.DeviceTokenDeleteRequest;
import com.odos.odos_server_v2.domain.push.dto.DeviceTokenUpsertRequest;
import com.odos.odos_server_v2.domain.push.service.DeviceTokenService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/push/device-tokens")
@Tag(name = "푸시", description = "앱 디바이스 토큰(FCM) 등록/삭제")
public class PushController {
  private final DeviceTokenService deviceTokenService;

  @Operation(summary = "디바이스 토큰 등록/갱신", description = "deviceId 기준 upsert. 로그인 후·토큰 갱신 시 호출.")
  @PostMapping
  public ApiResponse<Void> register(@Valid @RequestBody DeviceTokenUpsertRequest request) {
    deviceTokenService.upsert(CurrentUserContext.getCurrentMemberId(), request);
    return success(Message.DEVICE_TOKEN_REGISTERED);
  }

  @Operation(summary = "디바이스 토큰 삭제", description = "로그아웃 시 해당 기기 토큰을 비활성화.")
  @DeleteMapping
  public ApiResponse<Void> delete(@Valid @RequestBody DeviceTokenDeleteRequest request) {
    deviceTokenService.deactivate(CurrentUserContext.getCurrentMemberId(), request.deviceId());
    return success(Message.DEVICE_TOKEN_DELETED);
  }
}
