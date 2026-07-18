package com.odos.odos_server_v2.domain.vote.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.vote.dto.VoteDetailResponse;
import com.odos.odos_server_v2.domain.vote.dto.VoteSubmitRequest;
import com.odos.odos_server_v2.domain.vote.dto.VoteSummaryResponse;
import com.odos.odos_server_v2.domain.vote.service.VoteService;
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
import jakarta.validation.Valid;

@Tag(name = "Vote API", description = "사용자 투표 API")
@RestController
@RequestMapping("/votes")
@RequiredArgsConstructor
public class VoteController {
  private final VoteService voteService;

  @Operation(summary = "공개 투표 목록 조회", description = "결과 공개형(PUBLIC) 투표만 최신 시작일 순으로 조회합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "공개 투표 목록 조회 성공",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "투표 목록 조회 성공했습니다.",
                              "data": [{
                                "id": 1,
                                "title": "다음 챌린지 주제는?",
                                "selectionType": "SINGLE",
                                "voteType": "PUBLIC",
                                "startDate": "2026-07-16",
                                "endDate": "2026-07-20",
                                "voted": false
                              }]
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  public ApiResponse<List<VoteSummaryResponse>> getPublicVotes() {
    return ApiResponse.success(
        Message.GET_VOTE_LIST, voteService.getPublicVotes(CurrentUserContext.getCurrentMemberId()));
  }

  @Operation(
      summary = "오늘의 투표 조회",
      description = "KST 오늘이 시작일~종료일에 포함되는 투표를 조회합니다. 결과 비공개 관리자 설문도 참여를 위해 포함됩니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "오늘 진행 중인 투표 조회 성공. 대상이 없으면 data는 빈 배열",
        content = @Content(schema = @Schema(implementation = VoteSummaryResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/today")
  public ApiResponse<List<VoteSummaryResponse>> getTodayVotes() {
    return ApiResponse.success(
        Message.GET_TODAY_VOTES,
        voteService.getTodayVotes(CurrentUserContext.getCurrentMemberId()));
  }

  @Operation(
      summary = "사용자 투표 상세 조회",
      description = "공개 투표는 항목별 선택 비율만 제공합니다. 관리자 설문은 항목만 제공하고 결과는 숨깁니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "상세 조회 성공",
        content =
            @Content(
                examples = {
                  @ExampleObject(
                      name = "공개 투표",
                      value =
                          """
                          {
                            "message": "투표 상세 조회 성공했습니다.",
                            "data": {
                              "id": 1,
                              "title": "다음 챌린지 주제는?",
                              "selectionType": "SINGLE",
                              "voteType": "PUBLIC",
                              "startDate": "2026-07-16",
                              "endDate": "2026-07-20",
                              "voted": true,
                              "options": [
                                {"optionId": 1, "text": "운동", "percentage": 60.00},
                                {"optionId": 2, "text": "독서", "percentage": 40.00}
                              ]
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "관리자 설문",
                      description = "사용자 응답에는 percentage가 포함되지 않습니다.",
                      value =
                          """
                          {
                            "message": "투표 상세 조회 성공했습니다.",
                            "data": {
                              "id": 2,
                              "title": "서비스 만족도 조사",
                              "selectionType": "MULTIPLE",
                              "voteType": "ADMIN_SURVEY",
                              "startDate": "2026-07-16",
                              "endDate": "2026-07-20",
                              "voted": false,
                              "options": [
                                {"optionId": 3, "text": "사용하기 편해요"},
                                {"optionId": 4, "text": "기능이 다양해요"}
                              ]
                            }
                          }
                          """)
                })),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "투표 없음 (VOTE-001)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{voteId}")
  public ApiResponse<VoteDetailResponse> getDetail(
      @Parameter(description = "투표 ID", example = "1") @PathVariable Long voteId) {
    return ApiResponse.success(
        Message.GET_VOTE_DETAIL,
        voteService.getUserDetail(voteId, CurrentUserContext.getCurrentMemberId()));
  }

  @Operation(
      summary = "투표하기",
      description = "투표 기간 중 1회 참여할 수 있습니다. SINGLE은 항목 1개, MULTIPLE은 1개 이상을 전달합니다.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      description = "선택할 항목 ID 목록",
      content =
          @Content(
              schema = @Schema(implementation = VoteSubmitRequest.class),
              examples = {
                @ExampleObject(name = "단일 선택", value = "{\"optionIds\":[1]}"),
                @ExampleObject(name = "다중 선택", value = "{\"optionIds\":[1,2]}")
              }))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "투표 참여 성공",
        content = @Content(schema = @Schema(implementation = VoteDetailResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description =
            "투표 기간 아님(VOTE-004), 선택 방식 불일치(VOTE-006), 다른 투표의 항목 포함(VOTE-007)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "투표 또는 회원 없음 (VOTE-001, USER-003)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 참여한 투표 (VOTE-005)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/{voteId}/responses")
  public ApiResponse<VoteDetailResponse> submit(
      @PathVariable Long voteId, @Valid @RequestBody VoteSubmitRequest request) {
    return ApiResponse.success(
        Message.SUBMIT_VOTE,
        voteService.submit(voteId, CurrentUserContext.getCurrentMemberId(), request));
  }

  @Operation(summary = "투표 결과 조회", description = "PUBLIC 투표만 항목별 선택 비율을 조회할 수 있습니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "공개 투표 결과 조회 성공",
        content = @Content(schema = @Schema(implementation = VoteDetailResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자 설문 결과 비공개 (VOTE-008)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "투표 없음 (VOTE-001)")
  })
  @GetMapping("/{voteId}/results")
  public ApiResponse<VoteDetailResponse> getResult(@PathVariable Long voteId) {
    return ApiResponse.success(
        Message.GET_VOTE_RESULT,
        voteService.getPublicResult(voteId, CurrentUserContext.getCurrentMemberId()));
  }
}
