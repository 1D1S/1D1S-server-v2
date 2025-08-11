package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;

public class ChallengeDetailDto {
  private String description;
  private ParticipantStatus myStatus;
  private double participationRate;
  private double goalCompletionRate;
}
