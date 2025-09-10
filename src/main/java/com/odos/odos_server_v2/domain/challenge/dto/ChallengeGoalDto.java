package com.odos.odos_server_v2.domain.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ChallengeGoalDto {
  private Long challengeGoalId;
  private String content;
}
