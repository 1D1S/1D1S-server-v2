package com.odos.odos_server_v2.domain.vote.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminVoteOptionResponse {
  private Long optionId;
  private String text;
  private long selectedCount;
  private BigDecimal percentage;
  private List<AdminVoteVoterResponse> voters;
}
