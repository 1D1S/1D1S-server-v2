package com.odos.odos_server_v2.domain.widget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "위젯 요약 응답 — 스트릭 현황 + 오늘 일지를 아직 안 쓴 챌린지 목록")
public record WidgetSummaryResponse(
    @Schema(description = "내 스트릭 현황") StreakInfo streak,
    @Schema(description = "오늘 일지를 아직 작성하지 않은 진행중 챌린지 목록(마감 임박순, 최대 10개)")
        List<TodayChallenge> todayChallenges) {

  @Schema(description = "스트릭 현황")
  public record StreakInfo(
      @Schema(description = "현재 연속 작성 일수", example = "7") int currentStreak,
      @Schema(description = "오늘(KST) 일지 작성 여부", example = "true") boolean todayWritten) {}

  @Schema(description = "오늘 일지 미작성 챌린지")
  public record TodayChallenge(
      @Schema(description = "챌린지 ID", example = "1") Long challengeId,
      @Schema(description = "챌린지 제목", example = "30일 코딩 챌린지") String title,
      @Schema(description = "챌린지 썸네일 이미지 URL(없으면 null)", example = "https://cdn.example.com/a.png")
          String thumbnailImage) {}
}
