package com.odos.odos_server_v2.domain.diary.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;

@Builder
@Getter
public class DiaryInfoDto {
  private String createdAt;
  private String challengedDate;
  private Feeling feeling;
  private List<DiaryGoalDto> diaryGoal;
  private int achievementRate;
}
