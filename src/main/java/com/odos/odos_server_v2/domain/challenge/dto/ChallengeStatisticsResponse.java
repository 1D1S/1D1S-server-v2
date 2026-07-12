package com.odos.odos_server_v2.domain.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/** 특정 챌린지 통계 응답. */
@Schema(description = "특정 챌린지 통계 응답")
public record ChallengeStatisticsResponse(
    @Schema(description = "참여율(%) — 시작 전이면 -1", example = "42.5") double participationRate,
    @Schema(description = "이 챌린지의 완료된 목표 총수", example = "128") long completedGoalCount,
    @Schema(description = "챌린지 기간 동안 날짜별 일지 개수(연속 시계열, 빈 날짜는 0)")
        List<DiaryTrendPoint> diaryTrend) {

  /** 날짜별 일지 개수 한 점. */
  @Schema(description = "날짜별 일지 개수")
  public record DiaryTrendPoint(
      @Schema(description = "날짜(completedDate 기준)", example = "2026-07-01") LocalDate date,
      @Schema(description = "해당 날짜 일지 개수", example = "3") long count) {}
}
