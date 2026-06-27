package com.odos.odos_server_v2.domain.challenge.controller;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeDeleteAdminRequest;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeDeleteAdminResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.service.AdminChallengeService;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/challenges")
@Slf4j
@Tag(name = "Admin Challenge API", description = "관리자 챌린지 API")
public class AdminChallengeController {

  private final AdminChallengeService adminChallengeService;

  @Operation(
      summary = "관리자 챌린지 목록 조회",
      description =
          "카테고리, 진행 상태, 작성자, 정렬 기준 필터를 조합해 챌린지를 조회합니다. 필터는 중복 적용 가능합니다.\n\n"
              + "- status: ONGOING(진행중) / UPCOMING(시작전) / ENDED(종료) — 미입력 시 전체\n"
              + "- sortBy: latest(최신순, 기본값) / likes(좋아요순)")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
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
                              "message": "챌린지 목록 조회 성공했습니다.",
                              "data": {
                                "items": [
                                  {
                                    "challengeId": 1,
                                    "title": "매일 독서하기",
                                    "thumbnailImage": "https://odos-pic",
                                    "category": "BOOK",
                                    "challengeType": "PUBLIC",
                                    "startDate": "2026-06-01",
                                    "endDate": "2026-12-31",
                                    "status": "ONGOING",
                                    "host": {
                                      "memberId": 1,
                                      "nickname": "홍길동",
                                      "profileImg": "https://odos-pic"
                                    },
                                    "participantCnt": 5,
                                    "likeCnt": 10,
                                    "createdAt": "2026-06-01T10:00:00"
                                  }
                                ],
                                "pageInfo": {
                                  "page": 0,
                                  "size": 20,
                                  "totalElements": 100,
                                  "totalPages": 5,
                                  "hasNextPage": true
                                }
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 필터 요청",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "CHALLENGE_026",
                              "message": "관리자 챌린지 필터 요청 형식이 올바르지 않습니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "관리자가 아닌 경우",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "AUTH-001",
                              "message": "인증되지 않은 접근입니다."
                            }
                            """)))
  })
  @GetMapping("")
  public ApiResponse<OffsetPagination<ChallengeSummaryResponse>> getChallenges(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @Parameter(
              description = "진행 상태 필터 (ONGOING: 진행중, UPCOMING: 시작전, ENDED: 종료)",
              example = "ONGOING")
          @RequestParam(required = false)
          String status,
      @Parameter(description = "카테고리 필터", example = "BOOK") @RequestParam(required = false)
          Category category,
      @Parameter(description = "작성자 닉네임 검색 (부분 일치)", example = "홍길동")
          @RequestParam(required = false)
          String authorNickname,
      @Parameter(description = "정렬 기준 (latest: 최신순(기본), likes: 좋아요순)", example = "latest")
          @RequestParam(defaultValue = "latest")
          String sortBy) {
    Pageable pageable = PageRequest.of(page, size);
    OffsetPagination<ChallengeSummaryResponse> result =
        adminChallengeService.getChallenges(status, category, authorNickname, sortBy, pageable);
    return ApiResponse.success(Message.ADMIN_CHALLENGE_GET_ALL_SUCCESS, result);
  }

  @Operation(summary = "관리자 챌린지 삭제", description = "관리자가 부적절한 챌린지를 삭제 사유와 함께 소프트 삭제 처리합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "챌린지 삭제 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "챌린지 삭제 성공했습니다.",
                              "data": {
                                "challengeId": 1,
                                "challengeTitle": "매일 독서하기",
                                "challengeDeletedReason": "부적절한 내용이 포함되어 있습니다."
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "삭제 사유 누락",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "CHALLENGE_025",
                              "message": "챌린지 삭제 사유를 입력해야 합니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 챌린지",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "CHALLENGE_001",
                              "message": "챌린지를 찾을 수 없습니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "관리자가 아닌 경우",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "AUTH-001",
                              "message": "인증되지 않은 접근입니다."
                            }
                            """)))
  })
  @PatchMapping("/{challengeId}")
  public ApiResponse<ChallengeDeleteAdminResponse> deleteChallenge(
      @Parameter(description = "삭제할 챌린지 ID", example = "1") @PathVariable Long challengeId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "관리자 챌린지 삭제 요청",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = ChallengeDeleteAdminRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                  {
                                    "deleteReason": "부적절한 내용이 포함되어 있습니다."
                                  }
                                  """)))
          @RequestBody
          ChallengeDeleteAdminRequest request) {
    ChallengeDeleteAdminResponse result =
        adminChallengeService.deleteChallenge(challengeId, request);
    return ApiResponse.success(Message.ADMIN_CHALLENGE_DELETE_SUCCESS, result);
  }
}
