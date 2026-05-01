package com.odos.odos_server_v2.domain.notification.controller;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.notification.dto.NotificationEndpointDeleteRequest;
import com.odos.odos_server_v2.domain.notification.dto.NotificationEndpointResponse;
import com.odos.odos_server_v2.domain.notification.dto.NotificationEndpointUpsertRequest;
import com.odos.odos_server_v2.domain.notification.dto.NotificationPreferenceRequest;
import com.odos.odos_server_v2.domain.notification.dto.NotificationPreferenceResponse;
import com.odos.odos_server_v2.domain.notification.dto.NotificationResponse;
import com.odos.odos_server_v2.domain.notification.dto.UnreadCountResponse;
import com.odos.odos_server_v2.domain.notification.dto.WebPushPublicKeyResponse;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping("/endpoints")
  public ApiResponse<List<NotificationEndpointResponse>> getEndpoints() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_ENDPOINT_LIST_SUCCESS, notificationService.getEndpoints(memberId));
  }

  @PostMapping("/endpoints")
  public ApiResponse<NotificationEndpointResponse> upsertEndpoint(
      @RequestBody NotificationEndpointUpsertRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_ENDPOINT_UPSERT_SUCCESS,
        notificationService.upsertEndpoint(memberId, request));
  }

  @GetMapping
  public ApiResponse<OffsetPagination<NotificationResponse>> getMyNotifications(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_LIST_SUCCESS,
        notificationService.getMyNotifications(memberId, page, size));
  }

  @GetMapping("/unread-count")
  public ApiResponse<UnreadCountResponse> getUnreadCount() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_UNREAD_COUNT_SUCCESS, notificationService.getUnreadCount(memberId));
  }

  @PatchMapping("/{notificationId}/read")
  public ApiResponse<Void> markAsRead(@PathVariable Long notificationId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    notificationService.markAsRead(memberId, notificationId);
    return ApiResponse.success(Message.NOTIFICATION_READ_SUCCESS);
  }

  @PatchMapping("/read-all")
  public ApiResponse<Void> markAllAsRead() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    notificationService.markAllAsRead(memberId);
    return ApiResponse.success(Message.NOTIFICATION_READ_ALL_SUCCESS);
  }

  @GetMapping("/web-push/public-key")
  public ApiResponse<WebPushPublicKeyResponse> getWebPushPublicKey() {
    return ApiResponse.success(
        Message.NOTIFICATION_WEB_PUSH_PUBLIC_KEY_SUCCESS,
        notificationService.getWebPushPublicKey());
  }

  @DeleteMapping("/endpoints")
  public ApiResponse<Void> deleteEndpoint(@RequestBody NotificationEndpointDeleteRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    notificationService.deleteEndpoint(memberId, request);
    return ApiResponse.success(Message.NOTIFICATION_ENDPOINT_DELETE_SUCCESS);
  }

  @GetMapping("/preferences")
  public ApiResponse<NotificationPreferenceResponse> getPreferences() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_PREFERENCE_GET_SUCCESS, notificationService.getPreference(memberId));
  }

  @PutMapping("/preferences")
  public ApiResponse<NotificationPreferenceResponse> updatePreferences(
      @RequestBody NotificationPreferenceRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_PREFERENCE_UPDATE_SUCCESS,
        notificationService.updatePreference(memberId, request));
  }
}
