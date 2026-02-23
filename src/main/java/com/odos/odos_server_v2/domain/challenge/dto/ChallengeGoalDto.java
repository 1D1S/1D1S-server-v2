package com.odos.odos_server_v2.domain.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "챌린지 목표")
@Builder
@AllArgsConstructor
@Getter
public class ChallengeGoalDto {

  @Schema(description = "챌린지 목표 ID", example = "1")
  private Long challengeGoalId;

  @Schema(description = "목표 내용", example = "알고리즘 1문제 풀기")
  private String content;
}
