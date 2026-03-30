package com.odos.odos_server_v2.domain.member.controller;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.dto.MyPageDto;
import com.odos.odos_server_v2.domain.member.dto.NicknameRequest;
import com.odos.odos_server_v2.domain.member.dto.ProfileImageRequest;
import com.odos.odos_server_v2.domain.member.dto.SideBarDto;
import com.odos.odos_server_v2.domain.member.service.MemberService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
  private final MemberService memberService;

  @Operation(summary = "마이페이지 조회", description = "로그인한 회원의 마이페이지 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "마이페이지 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MyPageDto.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "마이페이지 조회 성공했습니다.",
                              "data": {
                                "nickname": "홍길동",
                                "profileUrl": "https://example.com/profile.jpg",
                                "email" : "1day1streak@naver.com",
                                "provider": "NAVER",
                                "streak": {
                                  "todayGoalCount": 3,
                                  "currentStreak": 5,
                                  "totalDiaryCount": 30,
                                  "totalGoalCount": 120,
                                  "currentMonthDiaryCount": 10,
                                  "currentMonthGoalCount": 40,
                                  "maxStreak": 15,
                                  "calendar": [
                                    { "date": "2025-09-01", "count": 2 },
                                    { "date": "2025-09-02", "count": 1 }
                                  ]
                                },
                                "challengeList": [
                                  {
                                    "challengeId": 1,
                                    "title": "30일 코딩 챌린지",
                                    "category": "DEV",
                                    "startDate": "2025-09-01",
                                    "endDate": "2025-09-30",
                                    "maxParticipantCnt": 10,
                                    "challengeType": "FIXED",
                                    "participantCnt": 5,
                                    "likeInfo": { "likedByMe": false, "likeCnt": 3 }
                                  }
                                ],
                                "diaryList": [
                                  {
                                    "id": 1,
                                    "title": "오늘의 일지",
                                    "content": "오늘은 알고리즘 문제를 풀었습니다.",
                                    "isPublic": true,
                                    "likeInfo": { "likedByMe": false, "likeCnt": 2 }
                                  }
                                ]
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                            """)))
  })
  @GetMapping("/my-page")
  public ApiResponse<MyPageDto> getMyPage(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(Message.GET_MYPAGE, memberService.getMyPage(memberId, pageable));
  }

  @Operation(summary = "사이드바 조회", description = "로그인한 회원의 사이드바 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "사이드바 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SideBarDto.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "사이드바 조회 성공했습니다.",
                              "data": {
                                "nickname": "홍길동",
                                "profileUrl": "https://example.com/profile.jpg",
                                "streakCount": 5,
                                "todayGoalCount": 3,
                                "challengeList": [
                                  {
                                    "challengeId": 1,
                                    "title": "30일 코딩 챌린지",
                                    "category": "DEV",
                                    "startDate": "2025-09-01",
                                    "endDate": "2025-09-30",
                                    "maxParticipantCnt": 10,
                                    "challengeType": "FIXED",
                                    "participantCnt": 5,
                                    "likeInfo": { "likedByMe": false, "likeCnt": 3 }
                                  }
                                ]
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                            """)))
  })
  @GetMapping("/side-bar")
  public ApiResponse<SideBarDto> getSideBar() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(Message.GET_SIDEBAR, memberService.getSideBar(memberId));
  }

  @Operation(summary = "닉네임 변경", description = "로그인한 회원의 닉네임을 변경한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "닉네임 변경 성공했습니다.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SideBarDto.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                            {
                                              "message": "닉네임 변경 성공했습니다."
                                            }
                                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                            { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                            { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                                            """)))
  })
  @PatchMapping("/nickname")
  public ApiResponse<Void> editNickname(@RequestBody NicknameRequest nicknameRequest) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    memberService.editNickname(memberId, nicknameRequest.getNickname());
    return ApiResponse.success(Message.UPDATE_NICKNAME);
  }

  @Operation(summary = "프로필 이미지 변경", description = "로그인한 회원의 프로필 이미지를 변경한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "프로필 이미지 변경 성공했습니다.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SideBarDto.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                            {
                                              "message": "프로필 이미지 변경 성공했습니다."
                                            }
                                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                            { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                            { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                                            """)))
  })
  @PatchMapping("/profile-image")
  public ApiResponse<Void> editProfileImage(@RequestBody ProfileImageRequest profileImageRequest) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    memberService.editProfileImage(memberId, profileImageRequest.getObjectKey());
    return ApiResponse.success(Message.UPDATE_PROFILE_IMAGE);
  }

  @Operation(summary = "다른 회원 프로필 조회", description = "특정 회원의 프로필 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "다른 회원 프로필 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MyPageDto.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "다른 회원 프로필 조회 성공했습니다.",
                              "data": {
                                "nickname": "홍길동",
                                "profileUrl": "https://example.com/profile.jpg",
                                "streak": {
                                  "todayGoalCount": 3,
                                  "currentStreak": 5,
                                  "totalDiaryCount": 30,
                                  "totalGoalCount": 120,
                                  "currentMonthDiaryCount": 10,
                                  "currentMonthGoalCount": 40,
                                  "maxStreak": 15,
                                  "calendar": [
                                    { "date": "2025-09-01", "count": 2 },
                                    { "date": "2025-09-02", "count": 1 }
                                  ]
                                },
                                "challengeList": [
                                  {
                                    "challengeId": 1,
                                    "title": "30일 코딩 챌린지",
                                    "category": "DEV",
                                    "startDate": "2025-09-01",
                                    "endDate": "2025-09-30",
                                    "maxParticipantCnt": 10,
                                    "challengeType": "FIXED",
                                    "participantCnt": 5,
                                    "likeInfo": { "likedByMe": false, "likeCnt": 3 }
                                  }
                                ],
                                "diaryList": [
                                  {
                                    "id": 1,
                                    "title": "오늘의 일지",
                                    "content": "오늘은 알고리즘 문제를 풀었습니다.",
                                    "isPublic": true,
                                    "likeInfo": { "likedByMe": false, "likeCnt": 2 }
                                  }
                                ]
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "비공개 프로필 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "USER-005", "message": "비공개 프로필입니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                            """)))
  })
  @GetMapping("/profile/{memberId}")
  public ApiResponse<MyPageDto> getOtherProfile(
      @PathVariable Long memberId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ApiResponse.success(
        Message.GET_OTHERS_PROFILE, memberService.getOtherMyPage(memberId, pageable));
  }
}
