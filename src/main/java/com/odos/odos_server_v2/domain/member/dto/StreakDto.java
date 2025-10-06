package com.odos.odos_server_v2.domain.member.dto;

import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StreakDto {
  private int todayGoalCount;
  private int currentStreak;
  private int totalDiaryCount;
  private int totalGoalCount;
  private int currentMonthDiaryCount;
  private int currentMonthGoalCount;
  private int maxStreak;
  List<CalendarStreakDto> calendar;
}
