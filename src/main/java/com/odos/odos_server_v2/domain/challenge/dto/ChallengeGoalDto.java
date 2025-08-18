package com.odos.odos_server_v2.domain.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ChallengeGoalDto {
  private Long challengeGoalId;
  private String content;
}
