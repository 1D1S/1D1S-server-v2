package com.odos.odos_server_v2.domain.vote.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminVoteVoterResponse {
  private Long memberId;
  private String nickname;
}
