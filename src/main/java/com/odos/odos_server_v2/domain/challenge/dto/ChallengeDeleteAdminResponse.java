package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeDeleteAdminResponse {
  private Long challengeId;
  private String challengeTitle;
  private String challengeDeletedReason;

  public static ChallengeDeleteAdminResponse from(Challenge challenge) {
    return ChallengeDeleteAdminResponse.builder()
        .challengeId(challenge.getId())
        .challengeTitle(challenge.getTitle())
        .challengeDeletedReason(challenge.getDeletedReason())
        .build();
  }
}
