package com.odos.odos_server_v2.domain.challenge.controller;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeRequest;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ParticipantResponse;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "챌린지", description = "챌린지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {
  private final ChallengeService challengeService;

  @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChallengeSummaryResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "챌린지 생성 성공했습니다.",
                              "data": {
                                "challengeId": 1,
                                "title": "30일 코딩 챌린지",
                                "category": "DEV",
                                "startDate": "2025-09-01",
                                "endDate": "2025-09-30",
                                "maxParticipantCnt": 10,
                                "challengeType": "FIXED",
                                "participantCnt": 1,
                                "likeInfo": { "likedByMe": false, "likeCnt": 0 }
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
  @PostMapping
  public ApiResponse<ChallengeSummaryResponse> createChallenge(
      @RequestBody ChallengeRequest challengeRequest) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.CREATE_CHALLENGE, challengeService.createChallenge(challengeRequest, memberId));
  }

  @Operation(summary = "챌린지 상세 조회", description = "챌린지 ID로 챌린지의 상세 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChallengeResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "챌린지 상세 조회 성공했습니다.",
                              "data": {
                                "challengeSummary": {
                                  "challengeId": 1,
                                  "title": "30일 코딩 챌린지",
                                  "category": "DEV",
                                  "startDate": "2025-09-01",
                                  "endDate": "2025-09-30",
                                  "maxParticipantCnt": 10,
                                  "challengeType": "FIXED",
                                  "participantCnt": 5,
                                  "likeInfo": { "likedByMe": true, "likeCnt": 12 }
                                },
                                "challengeDetail": {
                                  "description": "매일 1시간씩 코딩 공부를 진행합니다.",
                                  "myStatus": "PARTICIPANT",
                                  "participationRate": 75.0,
                                  "goalCompletionRate": 60.5
                                },
                                "challengeGoals": [
                                  { "challengeGoalId": 1, "content": "알고리즘 1문제 풀기" },
                                  { "challengeGoalId": 2, "content": "책 10페이지 읽기" }
                                ],
                                "participants": [
                                  {
                                    "memberId": 1,
                                    "participantId": 1,
                                    "nickname": "호스트닉네임",
                                    "profileImg": "https://example.com/profile.jpg",
                                    "status": "HOST"
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
        description = "챌린지 또는 회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "챌린지 없음",
                      value =
                          """
                          { "code": "CHALLENGE_001", "message": "챌린지를 찾을 수 없습니다." }
                          """),
                  @ExampleObject(
                      name = "회원 없음",
                      value =
                          """
                          { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                          """)
                }))
  })
  @GetMapping("/{challengeId}")
  public ApiResponse<ChallengeResponse> getChallenge(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_CHALLENGE, challengeService.getChallenge(challengeId, memberId));
  }

  @Operation(summary = "챌린지 참여 신청", description = "챌린지에 참여를 신청한다. 신청 시 달성할 목표 목록을 함께 전달한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 참여 신청 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ParticipantResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "챌린지 신청 성공했습니다.",
                              "data": {
                                "memberId": 2,
                                "participantId": 5,
                                "nickname": "홍길동",
                                "profileImg": "https://example.com/profile.jpg",
                                "status": "PENDING"
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
        description = "챌린지 또는 회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "챌린지 없음",
                      value =
                          """
                          { "code": "CHALLENGE_001", "message": "챌린지를 찾을 수 없습니다." }
                          """),
                  @ExampleObject(
                      name = "회원 없음",
                      value =
                          """
                          { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                          """)
                })),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 해당 챌린지에 신청한 상태",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_002", "message": "이미 해당 챌린지에 신청한 상태입니다." }
                            """)))
  })
  @PostMapping("/{challengeId}/participants")
  public ApiResponse<ParticipantResponse> applyParticipation(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @RequestBody List<String> goals) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.APPLY_CHALLENGE, challengeService.applyParticipant(challengeId, memberId, goals));
  }

  @Operation(summary = "참여 신청 수락", description = "챌린지 호스트가 특정 참여 신청을 수락한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "참여 신청 수락 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "참여자 수락 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근 또는 권한 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "인증되지 않은 접근",
                      value =
                          """
                          { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                          """),
                  @ExampleObject(
                      name = "권한 없음",
                      value =
                          """
                          { "code": "CHALLENGE_004", "message": "권한이 없습니다." }
                          """)
                })),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "참여자를 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_003", "message": "참여자를 찾을 수 없습니다." }
                            """)))
  })
  @PatchMapping("/participants/{participantId}/accept")
  public ApiResponse<Void> acceptParticipant(
      @Parameter(description = "참여자 ID") @PathVariable Long participantId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.acceptParticipant(participantId, memberId);
    return ApiResponse.success(Message.ACCEPT_PARTICIPANT);
  }

  @Operation(summary = "참여 신청 거절", description = "챌린지 호스트가 특정 참여 신청을 거절한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "참여 신청 거절 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "참여자 거절 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근 또는 권한 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "인증되지 않은 접근",
                      value =
                          """
                          { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                          """),
                  @ExampleObject(
                      name = "권한 없음",
                      value =
                          """
                          { "code": "CHALLENGE_004", "message": "권한이 없습니다." }
                          """)
                })),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "참여자를 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_003", "message": "참여자를 찾을 수 없습니다." }
                            """)))
  })
  @PatchMapping("/participants/{participantId}/reject")
  public ApiResponse<Void> rejectParticipant(
      @Parameter(description = "참여자 ID") @PathVariable Long participantId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.rejectParticipant(participantId, memberId);
    return ApiResponse.success(Message.REJECT_PARTICIPANT);
  }

  @Operation(summary = "랜덤 챌린지 조회", description = "지정한 개수만큼 랜덤으로 챌린지를 조회한다. 비로그인 상태로도 조회할 수 있다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "랜덤 챌린지 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChallengeSummaryResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "챌린지 랜덤 불러오기 성공했습니다.",
                              "data": [
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
                            """)))
  })
  @GetMapping("/random")
  public ApiResponse<List<ChallengeSummaryResponse>> randomChallenges(
      @Parameter(description = "조회할 챌린지 수 (기본값: 10)")
          @RequestParam(name = "size", defaultValue = "10")
          int size) {
    Long memberId = CurrentUserContext.getCurrentMemberIdOrNull();
    return ApiResponse.success(
        Message.GET_RANDOM_CHALLENGES, challengeService.getRandomChallenges(memberId, size));
  }

  @Operation(summary = "챌린지 탈퇴", description = "참여 중인 챌린지에서 탈퇴한다. 호스트는 탈퇴할 수 없다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 탈퇴 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "챌린지 탈퇴하기 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "챌린지 탈퇴 불가 (호스트 또는 이미 탈퇴 상태)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_007", "message": "챌린치 탈퇴가 불가능합니다." }
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
        description = "챌린지 또는 참여자를 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "챌린지 없음",
                      value =
                          """
                          { "code": "CHALLENGE_001", "message": "챌린지를 찾을 수 없습니다." }
                          """),
                  @ExampleObject(
                      name = "참여자 없음",
                      value =
                          """
                          { "code": "CHALLENGE_003", "message": "참여자를 찾을 수 없습니다." }
                          """)
                }))
  })
  @DeleteMapping("/{challengeId}/participants")
  public ApiResponse<Void> leaveChallenge(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.leaveChallenge(memberId, challengeId);
    return ApiResponse.success(Message.LEAVE_CHALLENGE);
  }

  @Operation(summary = "챌린지 목록 조회", description = "커서 기반 페이징으로 챌린지 목록을 조회한다. 키워드로 검색할 수 있다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "챌린지 리스트 불러오기 성공했습니다.",
                              "data": {
                                "items": [
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
                                "pageInfo": {
                                  "limit": 10,
                                  "hasNextPage": true,
                                  "nextCursor": "Y3Vyc29yOjE="
                                }
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
                            """)))
  })
  @GetMapping
  public ApiResponse<Pagination<ChallengeSummaryResponse>> challengeList(
      @Parameter(description = "페이지당 조회 수 (기본값: 10)")
          @RequestParam(name = "limit", defaultValue = "10")
          int limit,
      @Parameter(description = "다음 페이지 커서값") @RequestParam(name = "cursor", required = false)
          String cursor,
      @Parameter(description = "검색 키워드") @RequestParam(name = "keyword", required = false)
          String keyword) {

    Long memberId = CurrentUserContext.getCurrentMemberId();
    Pagination<ChallengeSummaryResponse> page =
        challengeService.getChallengeList(memberId, limit, cursor, keyword);

    return ApiResponse.success(Message.GET_CHALLENGE_LIST, page);
  }

  @Operation(summary = "챌린지 좋아요", description = "챌린지에 좋아요를 추가한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 좋아요 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "챌린지 좋아요 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "이미 좋아요를 누른 챌린지",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_005", "message": "이미 좋아요를 눌렀습니다." }
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
        description = "챌린지 또는 회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "챌린지 없음",
                      value =
                          """
                          { "code": "CHALLENGE_001", "message": "챌린지를 찾을 수 없습니다." }
                          """),
                  @ExampleObject(
                      name = "회원 없음",
                      value =
                          """
                          { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                          """)
                }))
  })
  @PostMapping("/{challengeId}/likes")
  public ApiResponse<Void> addChallengeLike(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.addChallengeLike(memberId, challengeId);
    return ApiResponse.success(Message.ADD_CHALLENGE_LIKE);
  }

  @Operation(summary = "챌린지 좋아요 취소", description = "챌린지에 누른 좋아요를 취소한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 좋아요 취소 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "챌린지 좋아요 취소 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "좋아요 이력이 없는 챌린지",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_006", "message": "챌린지 좋아요 정보가 없습니다." }
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
                            """)))
  })
  @DeleteMapping("/{challengeId}/likes")
  public ApiResponse<Void> cancelChallengeLike(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.cancelChallengeLike(memberId, challengeId);
    return ApiResponse.success(Message.CANCEL_CHALLENGE_LIKE);
  }

  @Operation(summary = "특정 회원의 챌린지 목록 조회", description = "특정 회원이 참여 중인 챌린지 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "회원 챌린지 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChallengeSummaryResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "진행중인 챌린지 불러오기 성공했습니다.",
                              "data": [
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
  @GetMapping("/member")
  public ApiResponse<List<ChallengeSummaryResponse>> memberChallenges(
      @Parameter(description = "조회할 회원 ID") @RequestParam("memberId") Long memberId) {
    Long currentMemberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_CHALLENGES_BY_MEMBER,
        challengeService.getMemberChallenge(currentMemberId, memberId));
  }
}
