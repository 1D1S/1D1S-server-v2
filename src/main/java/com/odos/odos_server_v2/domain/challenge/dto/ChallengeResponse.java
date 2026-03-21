package com.odos.odos_server_v2.domain.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "챌린지 상세 응답")
@Builder
@AllArgsConstructor
@Getter
public class ChallengeResponse {

  @Schema(description = "챌린지 요약 정보")
  private ChallengeSummaryResponse challengeSummary;

  @Schema(description = "챌린지 상세 정보")
  private ChallengeDetailDto challengeDetail;

  @Schema(description = "챌린지 목표 목록")
  private List<ChallengeGoalDto> challengeGoals;

  @Schema(description = "챌린지 참여자 목록")
  private List<ParticipantResponse> participants;
}
