package com.odos.odos_server_v2.domain.diary.dto;

import lombok.Builder;
import lombok.Getter;

import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;

@Getter
@Builder
public class DiaryGoalDto {
  private Long challengeGoalId;
  private String challengeGoalName;
  private Boolean isAchieved;

  public static DiaryGoalDto from(DiaryGoal goal) {
    return DiaryGoalDto.builder()
        .challengeGoalId(goal.getChallengeGoal().getId())
        .challengeGoalName(goal.getChallengeGoal().getContent())
        .isAchieved(goal.getIsCompleted())
        .build();
  }
}
