package com.odos.odos_server_v2.domain.challenge.controller;

import com.odos.odos_server_v2.domain.challenge.dto.*;
import com.odos.odos_server_v2.domain.challenge.service.OfficialChallengeService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공식 챌린지", description = "공식 챌린지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/official-challenges")
public class OfficialChallengeController {

  private final OfficialChallengeService officialChallengeService;

  @Operation(
      summary = "공식 챌린지 생성",
      description =
          """
          새로운 공식 챌린지를 생성한다.

          - 공식 챌린지는 언제나 단체 챌린지이며 인원수 제한이 없다.
          - 챌린지 카테고리는 다음에서 선택한다. DEV/EXERCISE/BOOK/DIET/HEALTH/HOBBY/LANGUAGE/SELF_DEV/ETC
          - 챌린지 목표는 다음에서 선택한다. FIXED(고정형)/FLEXIBLE(유연형)
          """)
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "공식 챌린지 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChallengeSummaryResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "공식 챌린지 생성 성공했습니다.",
                              "data": {
                                "challengeId": 1,
                                "title": "30일 코딩 챌린지",
                                "thumbnailImage": "https://..",
                                "category": "DEV",
                                "startDate": "2025-09-01",
                                "endDate": "2025-09-30",
                                "participationType": "GROUP",
                                "maxParticipantCnt": null,
                                "challengeType": "OFFICIAL",
                                "participantCnt": 1,
                                "likeInfo": { "likedByMe": false, "likeCnt": 0 },
                                "randomParticipants": [],
                                "deleted": false
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
  public ApiResponse<ChallengeSummaryResponse> createOfficialChallenge(
      @RequestBody OfficialChallengeRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.CREATE_OFFICIAL_CHALLENGE,
        officialChallengeService.createOfficialChallenge(request, memberId));
  }

  @Operation(
      summary = "공식 챌린지 수정",
      description =
          """
          공식 챌린지를 수정한다. 호스트만 수정 가능하다.

          변경할 필드만 요청에 포함하고, 변경하지 않는 필드는 생략한다.
          """)
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "공식 챌린지 수정 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChallengeSummaryResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "공식 챌린지 수정 성공했습니다.",
                              "data": {
                                "challengeId": 1,
                                "title": "수정된 챌린지",
                                "challengeType": "OFFICIAL"
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "공식 챌린지가 아님",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_017", "message": "공식 챌린지가 아닙니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "수정 권한 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_008", "message": "챌린지 수정 권한이 없습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "챌린지 또는 회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_001", "message": "챌린지를 찾을 수 없습니다." }
                            """)))
  })
  @PatchMapping("/{challengeId}")
  public ApiResponse<ChallengeSummaryResponse> editOfficialChallenge(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @RequestBody OfficialChallengeEditRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.EDIT_OFFICIAL_CHALLENGE,
        officialChallengeService.editOfficialChallenge(challengeId, request, memberId));
  }

  @Operation(
      summary = "공식 챌린지 삭제",
      description = "공식 챌린지를 삭제한다. 호스트만 삭제 가능하며 챌린지 내 모든 일지도 함께 삭제된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "공식 챌린지 삭제 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "공식 챌린지 삭제 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "공식 챌린지가 아님",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_017", "message": "공식 챌린지가 아닙니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "삭제 권한 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_008", "message": "챌린지 수정 권한이 없습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "챌린지를 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "CHALLENGE_001", "message": "챌린지를 찾을 수 없습니다." }
                            """)))
  })
  @DeleteMapping("/{challengeId}")
  public ApiResponse<Void> deleteOfficialChallenge(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    officialChallengeService.deleteOfficialChallenge(challengeId, memberId);
    return ApiResponse.success(Message.DELETE_OFFICIAL_CHALLENGE);
  }
}
