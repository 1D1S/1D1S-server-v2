package com.odos.odos_server_v2.domain.challenge.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ChallengeResponse {
  private ChallengeSummaryResponse challengeSummary;
  private ChallengeDetailDto challengeDetail;
  private List<ChallengeGoalDto> challengeGoals;
  private List<ParticipantResponse> participants;
}
