package com.odos.odos_server_v2.domain.vote.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.odos.odos_server_v2.domain.vote.dto.AdminVoteDetailResponse;
import com.odos.odos_server_v2.domain.vote.dto.VoteCreateRequest;
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

@Tag(name = "Admin Vote API", description = "관리자 투표 API (ADMIN 권한 필요)")
@RestController
@RequestMapping("/admin/votes")
@RequiredArgsConstructor
public class AdminVoteController {
  private final VoteService voteService;

  @Operation(
      summary = "투표 등록",
      description =
          "selectionType은 SINGLE 또는 MULTIPLE, voteType은 결과 공개형 PUBLIC 또는 결과 비공개형 ADMIN_SURVEY입니다. "
              + "항목은 2개 이상이어야 하며 텍스트와 기간은 중복·역전 여부를 검증합니다.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content =
          @Content(
              schema = @Schema(implementation = VoteCreateRequest.class),
              examples =
                  @ExampleObject(
                      value =
                          """
                          {
                            "title": "다음 챌린지 주제는?",
                            "selectionType": "SINGLE",
                            "options": ["운동", "독서", "미라클 모닝"],
                            "voteType": "PUBLIC",
                            "startDate": "2026-07-18",
                            "endDate": "2026-07-25"
                          }
                          """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "투표 등록 성공",
        content = @Content(schema = @Schema(implementation = VoteSummaryResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "필수값/형식 오류, 기간 역전(VOTE-002), 중복 항목(VOTE-003)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자가 아닌 회원 (USER-009)")
  })
  @PostMapping
  public ApiResponse<VoteSummaryResponse> create(@Valid @RequestBody VoteCreateRequest request) {
    return ApiResponse.success(Message.CREATE_VOTE, voteService.create(request));
  }

  @Operation(summary = "관리자 투표 전체 목록 조회", description = "PUBLIC과 ADMIN_SURVEY 투표를 모두 조회합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "전체 투표 목록 조회 성공. 최신 시작일 순",
        content = @Content(schema = @Schema(implementation = VoteSummaryResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자가 아닌 회원 (USER-009)")
  })
  @GetMapping
  public ApiResponse<List<VoteSummaryResponse>> getAll() {
    return ApiResponse.success(Message.GET_VOTE_LIST, voteService.getAllVotesForAdmin());
  }

  @Operation(
      summary = "관리자 투표 상세 조회",
      description =
          "각 투표 옵션별 선택 수·비율과 해당 옵션을 선택한 회원을 조회합니다. 다중 선택 투표에서는 한 회원이 여러 옵션의 voters에 포함될 수 있습니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "관리자 상세 조회 성공",
        content =
            @Content(
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "투표 상세 조회 성공했습니다.",
                              "data": {
                                "id": 1,
                                "title": "다음 챌린지 주제는?",
                                "selectionType": "MULTIPLE",
                                "voteType": "PUBLIC",
                                "startDate": "2026-07-18",
                                "endDate": "2026-07-25",
                                "participantCount": 2,
                                "options": [{
                                  "optionId": 1,
                                  "text": "운동",
                                  "selectedCount": 1,
                                  "percentage": 50.00,
                                  "voters": [{
                                    "memberId": 10,
                                    "nickname": "오도스"
                                  }]
                                }]
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "투표 없음 (VOTE-001)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자가 아닌 회원 (USER-009)")
  })
  @GetMapping("/{voteId}")
  public ApiResponse<AdminVoteDetailResponse> getDetail(
      @Parameter(description = "투표 ID", example = "1") @PathVariable Long voteId) {
    return ApiResponse.success(Message.GET_VOTE_DETAIL, voteService.getAdminDetail(voteId));
  }
}
