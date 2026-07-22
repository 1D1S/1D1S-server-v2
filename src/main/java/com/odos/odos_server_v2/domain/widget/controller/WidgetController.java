package com.odos.odos_server_v2.domain.widget.controller;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.widget.dto.WidgetSummaryResponse;
import com.odos.odos_server_v2.domain.widget.service.WidgetService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Widget API", description = "앱 위젯 전용 API (네이티브 토큰 Bearer 인증)")
@RestController
@RequestMapping("/widget")
@RequiredArgsConstructor
public class WidgetController {

  private final WidgetService widgetService;

  @Operation(
      summary = "위젯 요약 조회",
      description =
          "위젯 2종(스트릭 현황 / 오늘의 챌린지)에 필요한 데이터를 한 번에 반환한다. "
              + "Authorization: Bearer <accessToken> 로 호출한다. "
              + "todayChallenges 는 진행중이면서 승인 참여(HOST/PARTICIPANT)이고 "
              + "오늘(KST) 일지를 아직 쓰지 않은 챌린지만, 마감 임박순(endDate asc, 무기한은 뒤로 / "
              + "동률이면 challengeId asc)으로 최대 10개 반환한다. "
              + "오늘 판정은 모두 KST 기준이며 스트릭 계산과 동일하다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "위젯 요약 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WidgetSummaryResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "위젯 요약 조회 성공했습니다.",
                              "data": {
                                "streak": {
                                  "currentStreak": 7,
                                  "todayWritten": false
                                },
                                "todayChallenges": [
                                  {
                                    "challengeId": 1,
                                    "title": "30일 코딩 챌린지",
                                    "thumbnailImage": "https://cdn.example.com/a.png"
                                  }
                                ]
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "토큰 없음/만료/유효하지 않음 — 위젯은 빈 상태로 처리하면 된다",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "ACCESS_TOKEN_EXPIRED" }
                            """)))
  })
  @GetMapping("/summary")
  public ApiResponse<WidgetSummaryResponse> getSummary() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(Message.GET_WIDGET_SUMMARY, widgetService.getSummary(memberId));
  }
}
