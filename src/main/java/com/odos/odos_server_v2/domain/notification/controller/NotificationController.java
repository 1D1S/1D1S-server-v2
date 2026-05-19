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
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "알림", description = "알림 및 웹 푸시 구독 관련 API / 메시지 내용은 서버에서 동적으로 설정해서 response해드립니다.")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(
      summary = "웹 푸시 공개키 조회",
      description =
          "1단계) 브라우저 웹 푸시 구독에 필요한 VAPID 공개키를 조회함 \n"
              + "브라우저에서 PushManager.subscribe()를 호출하려면 서버의 VAPID 공개키가 필요함.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                schema = @Schema(implementation = WebPushPublicKeyResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                  {
                    "message": "웹 푸시 공개키 조회에 성공했습니다.",
                    "data": {
                      "publicKey": "BElxExamplePublicKeyForVapid"
                    }
                  }
                  """)))
  })
  @GetMapping("/web-push/public-key")
  public ApiResponse<WebPushPublicKeyResponse> getWebPushPublicKey() {
    return ApiResponse.success(
        Message.NOTIFICATION_WEB_PUSH_PUBLIC_KEY_SUCCESS,
        notificationService.getWebPushPublicKey());
  }

  @Operation(
      summary = "알림 구독 endpoint 등록/갱신",
      description = "2단계) 로그인한 사용자의 웹 푸시 구독 endpoint 정보를 등록하거나 갱신함.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "웹 푸시 구독 endpoint 등록/갱신 요청",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NotificationEndpointUpsertRequest.class),
              examples =
                  @ExampleObject(
                      name = "등록/갱신 요청 예시",
                      value =
                          """
                                  {
                                    "endpointUrl": "https://fcm.googleapis.com/fcm/send/eXampleEndpoint",
                                    "p256dh": "BOr4EExampleP256DH",
                                    "authSecret": "0xyZAExampleSecret"
                                  }
                                  """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "등록/갱신 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NotificationEndpointResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                          {
                                            "message": "알림 엔드포인트 등록/수정에 성공했습니다.",
                                            "data": {
                                              "id": 1,
                                              "endpointUrl": "https://fcm.googleapis.com/fcm/send/eXampleEndpoint",
                                              "isActive": true,
                                              "lastSeenAt": "2026-05-01T12:30:00"
                                            }
                                          }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/endpoints")
  public ApiResponse<NotificationEndpointResponse> upsertEndpoint(
      @RequestBody NotificationEndpointUpsertRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_ENDPOINT_UPSERT_SUCCESS,
        notificationService.upsertEndpoint(memberId, request));
  }

  @Operation(summary = "알림 수신 설정 조회", description = "3단계) 로그인한 사용자의 알림 수신 설정을 조회.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                schema = @Schema(implementation = NotificationPreferenceResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                  {
                    "message": "알림 설정 조회에 성공했습니다.",
                    "data": {
                      "pushEnabled": true,
                      "friendEnabled": true,
                      "diaryEnabled": false,
                      "challengeEnabled": true
                    }
                  }
                  """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/preferences")
  public ApiResponse<NotificationPreferenceResponse> getPreferences() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_PREFERENCE_GET_SUCCESS, notificationService.getPreference(memberId));
  }

  @Operation(
      summary = "알림 수신 설정 변경",
      description = "4단계) 로그인한 사용자의 알림 수신 설정을 변경한다. 기본값은 모두 false로 되어있음.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "알림 수신 설정 변경 요청",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NotificationPreferenceRequest.class),
              examples =
                  @ExampleObject(
                      value =
                          """
                                  {
                                    "pushEnabled": true,
                                    "friendEnabled": true,
                                    "diaryEnabled": false,
                                    "challengeEnabled": true
                                  }
                                  """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "설정 변경 성공",
        content =
            @Content(
                schema = @Schema(implementation = NotificationPreferenceResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                  {
                    "message": "알림 설정 변경에 성공했습니다.",
                    "data": {
                      "pushEnabled": true,
                      "friendEnabled": true,
                      "diaryEnabled": false,
                      "challengeEnabled": true
                    }
                  }
                  """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/preferences")
  public ApiResponse<NotificationPreferenceResponse> updatePreferences(
      @RequestBody NotificationPreferenceRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_PREFERENCE_UPDATE_SUCCESS,
        notificationService.updatePreference(memberId, request));
  }

  @Operation(summary = "내 알림 목록 조회", description = "로그인한 사용자의 알림 목록을 offset 페이징으로 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OffsetPagination.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                          {
                                            "message": "알림 목록 조회에 성공했습니다.",
                                            "data": {
                                              "items": [
                                                {
                                                  "id": 1,
                                                  "category": "FRIEND",
                                                  "type": "FRIEND_REQUEST",
                                                  "message": "mike님이 친구 신청을 보냈어요.",
                                                  "targetType": "MEMBER",
                                                  "targetId": 15,
                                                  "isRead": false,
                                                  "groupedCount": 1,
                                                  "actorId": 15,
                                                  "actorNickname": "mike",
                                                  "actorProfileUrl": "https://cdn.example.com/profile/mike.jpg",
                                                  "createdAt": "2026-05-01T09:00:00"
                                                },
                                                {
                                                  "id": 2,
                                                  "category": "DIARY",
                                                  "type": "DIARY_COMMENT",
                                                  "message": "anna님이 내 일지에 댓글을 남겼어요.",
                                                  "targetType": "DIARY",
                                                  "targetId": 99,
                                                  "isRead": true,
                                                  "groupedCount": 1,
                                                  "actorId": 21,
                                                  "actorNickname": "anna",
                                                  "actorProfileUrl": "https://cdn.example.com/profile/anna.jpg",
                                                  "createdAt": "2026-05-01T08:30:00"
                                                }
                                              ],
                                              "currentPage": 0,
                                              "size": 20,
                                              "hasNext": true
                                            }
                                          }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  public ApiResponse<OffsetPagination<NotificationResponse>> getMyNotifications(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_LIST_SUCCESS,
        notificationService.getMyNotifications(memberId, page, size));
  }

  @Operation(
      summary = "읽지 않은 알림 개수 조회",
      description = "(선택) 명세에는 없으나 만들었으나 패쓰하셔도 됩니다. 로그인한 사용자의 읽지 않은 알림 수를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                schema = @Schema(implementation = UnreadCountResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                  {
                    "message": "읽지 않은 알림 개수 조회에 성공했습니다.",
                    "data": {
                      "unreadCount": 7
                    }
                  }
                  """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/unread-count")
  public ApiResponse<UnreadCountResponse> getUnreadCount() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_UNREAD_COUNT_SUCCESS, notificationService.getUnreadCount(memberId));
  }

  @Operation(summary = "알림 단건 읽음 처리", description = "특정 알림을 읽음 상태로 변경한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "처리 성공",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
            {
              "message": "알림 읽음 처리에 성공했습니다."
            }
            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PatchMapping("/{notificationId}/read")
  public ApiResponse<Void> markAsRead(@PathVariable Long notificationId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    notificationService.markAsRead(memberId, notificationId);
    return ApiResponse.success(Message.NOTIFICATION_READ_SUCCESS);
  }

  @Operation(
      summary = "알림 구독 endpoint 목록 조회",
      description = "5단계) 로그인한 사용자의 웹 푸시 구독 endpoint 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                array =
                    @ArraySchema(
                        schema = @Schema(implementation = NotificationEndpointResponse.class)),
                examples =
                    @ExampleObject(
                        value =
                            """
                                          {
                                            "message": "알림 엔드포인트 목록 조회에 성공했습니다.",
                                            "data": [
                                              {
                                                "id": 1,
                                                "endpointUrl": "https://fcm.googleapis.com/fcm/send/eXampleEndpoint1",
                                                "isActive": true,
                                                "lastSeenAt": "2026-05-01T12:30:00"
                                              }
                                            ]
                                          }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/endpoints")
  public ApiResponse<List<NotificationEndpointResponse>> getEndpoints() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.NOTIFICATION_ENDPOINT_LIST_SUCCESS, notificationService.getEndpoints(memberId));
  }

  @Operation(summary = "알림 전체 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 상태로 변경한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "처리 성공",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
            {
              "message": "알림 전체 읽음 처리에 성공했습니다."
            }
            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PatchMapping("/read-all")
  public ApiResponse<Void> markAllAsRead() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    notificationService.markAllAsRead(memberId);
    return ApiResponse.success(Message.NOTIFICATION_READ_ALL_SUCCESS);
  }

  @Operation(summary = "알림 구독 endpoint 삭제", description = "로그인한 사용자의 특정 웹 푸시 구독 endpoint를 삭제한다.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "삭제할 웹 푸시 endpoint 정보",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NotificationEndpointDeleteRequest.class),
              examples =
                  @ExampleObject(
                      value =
                          """
                                  {
                                    "endpointUrl": "https://fcm.googleapis.com/fcm/send/eXampleEndpoint"
                                  }
                                  """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "삭제 성공",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
            {
              "message": "알림 엔드포인트 삭제에 성공했습니다."
            }
            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/endpoints")
  public ApiResponse<Void> deleteEndpoint(@RequestBody NotificationEndpointDeleteRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    notificationService.deleteEndpoint(memberId, request);
    return ApiResponse.success(Message.NOTIFICATION_ENDPOINT_DELETE_SUCCESS);
  }
}
