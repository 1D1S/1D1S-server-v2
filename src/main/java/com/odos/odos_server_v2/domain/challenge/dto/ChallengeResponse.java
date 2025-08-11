package com.odos.odos_server_v2.domain.challenge.dto;

import java.util.List;

public class ChallengeResponse {
  private ChallengeSummaryResponse challengeSummary;
  private ChallengeDetailDto challengeDetail;
  private List<ChallengeGoalDto> challengeGoals;
  private List<ParticipantDto> participants;
}
