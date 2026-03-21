package com.odos.odos_server_v2.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "스트릭 통계 정보")
@AllArgsConstructor
@Getter
public class StreakDto {

  @Schema(description = "오늘 달성해야 할 목표 수 (현재 참여 중인 챌린지 기준)", example = "3")
  private int todayGoalCount;

  @Schema(description = "현재 연속 일지 작성 일수", example = "5")
  private int currentStreak;

  @Schema(description = "전체 일지 작성 수", example = "30")
  private int totalDiaryCount;

  @Schema(description = "전체 목표 달성 수", example = "120")
  private int totalGoalCount;

  @Schema(description = "이번 달 일지 작성 수", example = "10")
  private int currentMonthDiaryCount;

  @Schema(description = "이번 달 목표 달성 수", example = "40")
  private int currentMonthGoalCount;

  @Schema(description = "최대 연속 일지 작성 일수", example = "15")
  private int maxStreak;

  @Schema(description = "이번 달 날짜별 일지 작성 현황")
  List<CalendarStreakDto> calendar;
}
