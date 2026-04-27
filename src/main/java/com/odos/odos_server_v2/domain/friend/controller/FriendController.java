package com.odos.odos_server_v2.domain.friend.controller;

import com.odos.odos_server_v2.domain.friend.dto.*;
import com.odos.odos_server_v2.domain.friend.service.FriendService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "친구", description = "친구 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {

  private final FriendService friendService;

  @Operation(summary = "친구 신청", description = "상대방에게 친구 신청을 보낸다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "친구 신청 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"친구 신청을 보냈습니다.\" }")})),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      value = "{ \"code\": \"USER-003\", \"message\": \"회원을 찾을 수 없습니다.\" }")
                }))
  })
  @PostMapping("/request")
  public ApiResponse<Message> sendFriendRequest(@RequestBody FriendRequestDto request) {
    friendService.sendFriendRequest(request.getToMemberId());
    return ApiResponse.success(Message.FRIEND_REQUEST_SEND);
  }

  @Operation(summary = "친구 신청 취소", description = "보낸 친구 신청을 취소한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "친구 신청 취소 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"친구 신청을 취소했습니다.\" }")}))
  })
  @DeleteMapping("/request/{requestId}")
  public ApiResponse<Message> cancelFriendRequest(@PathVariable Long requestId) {
    friendService.cancelFriendRequest(requestId);
    return ApiResponse.success(Message.FRIEND_REQUEST_CANCEL);
  }

  @Operation(summary = "친구 신청 수락", description = "받은 친구 신청을 수락한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "친구 신청 수락 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"친구 신청을 수락했습니다.\" }")}))
  })
  @PostMapping("/request/{requestId}/accept")
  public ApiResponse<Message> acceptFriendRequest(@PathVariable Long requestId) {
    friendService.acceptFriendRequest(requestId);
    return ApiResponse.success(Message.FRIEND_REQUEST_ACCEPT);
  }

  @Operation(summary = "친구 신청 거절", description = "받은 친구 신청을 거절한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "친구 신청 거절 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"친구 신청을 거절했습니다.\" }")}))
  })
  @PostMapping("/request/{requestId}/reject")
  public ApiResponse<Message> rejectFriendRequest(@PathVariable Long requestId) {
    friendService.rejectFriendRequest(requestId);
    return ApiResponse.success(Message.FRIEND_REQUEST_REJECT);
  }

  @Operation(summary = "친구 목록 조회", description = "내 친구 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "친구 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      value =
                          "{ \"data\": [{ \"memberId\": 2, \"nickname\": \"홍길동\", \"profileUrl\": \"https://...\" }] }")
                }))
  })
  @GetMapping
  public ApiResponse<List<FriendResponseDto>> getFriendList() {
    return ApiResponse.success(Message.GET_FRIEND_LIST, friendService.getFriendList());
  }

  @Operation(summary = "받은 친구 신청 목록", description = "내가 받은 친구 신청 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "받은 친구 신청 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      value =
                          "{ \"data\": [{ \"requestId\": 1, \"fromMemberId\": 2, \"fromMemberNickname\": \"홍길동\", ... }] }")
                }))
  })
  @GetMapping("/requests/received")
  public ApiResponse<List<FriendRequestResponseDto>> getReceivedFriendRequests() {
    return ApiResponse.success(
        Message.GET_FRIEND_REQUESTS, friendService.getReceivedFriendRequests());
  }

  @Operation(summary = "보낸 친구 신청 목록", description = "내가 보낸 친구 신청 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "보낸 친구 신청 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      value =
                          "{ \"data\": [{ \"requestId\": 1, \"toMemberId\": 2, \"toMemberNickname\": \"홍길동\", ... }] }")
                }))
  })
  @GetMapping("/requests/sent")
  public ApiResponse<List<FriendRequestResponseDto>> getSentFriendRequests() {
    return ApiResponse.success(Message.GET_FRIEND_REQUESTS, friendService.getSentFriendRequests());
  }

  @Operation(summary = "친구 삭제", description = "친구 관계를 해제한다. (언팔로우)")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "친구 삭제 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"친구를 삭제했습니다.\" }")}))
  })
  @DeleteMapping("/{friendMemberId}")
  public ApiResponse<Message> deleteFriend(@PathVariable Long friendMemberId) {
    friendService.deleteFriend(friendMemberId);
    return ApiResponse.success(Message.FRIEND_DELETE);
  }

  @Operation(summary = "차단", description = "회원을 차단한다. 친구 관계가 있으면 자동 해제된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "차단 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"회원을 차단했습니다.\" }")}))
  })
  @PostMapping("/block")
  public ApiResponse<Message> blockMember(@RequestBody BlockRequestDto request) {
    friendService.blockMember(request.getBlockedMemberId());
    return ApiResponse.success(Message.MEMBER_BLOCK);
  }

  @Operation(summary = "차단 해제", description = "차단한 회원의 차단을 해제한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "차단 해제 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"차단을 해제했습니다.\" }")}))
  })
  @DeleteMapping("/block/{blockedMemberId}")
  public ApiResponse<Message> unblockMember(@PathVariable Long blockedMemberId) {
    friendService.unblockMember(blockedMemberId);
    return ApiResponse.success(Message.MEMBER_UNBLOCK);
  }

  @Operation(summary = "차단 목록 조회", description = "내가 차단한 회원 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "차단 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      value = "{ \"data\": [{ \"memberId\": 2, \"nickname\": \"홍길동\", ... }] }")
                }))
  })
  @GetMapping("/block")
  public ApiResponse<List<FriendResponseDto>> getBlockList() {
    return ApiResponse.success(Message.GET_BLOCK_LIST, friendService.getBlockList());
  }

  @Operation(summary = "회원 관계 상태 조회", description = "특정 회원과의 친구 관계 상태를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "관계 상태 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      value = "{ \"data\": { \"memberId\": 2, \"relationStatus\": \"FRIEND\" } }")
                }))
  })
  @GetMapping("/relation/{memberId}")
  public ApiResponse<MemberRelationResponseDto> getMemberRelation(@PathVariable Long memberId) {
    return ApiResponse.success(
        Message.GET_MEMBER_RELATION, friendService.getMemberRelation(memberId));
  }
}
