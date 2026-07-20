package com.odos.odos_server_v2.domain.vote.dto;

import com.odos.odos_server_v2.domain.vote.entity.Vote;
import com.odos.odos_server_v2.domain.vote.entity.VoteSelectionType;
import com.odos.odos_server_v2.domain.vote.entity.VoteType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteSummaryResponse {
  private Long id;
  private String title;
  private VoteSelectionType selectionType;
  private VoteType voteType;
  private LocalDate startDate;
  private LocalDate endDate;
  private boolean voted;

  public static VoteSummaryResponse from(Vote vote, boolean voted) {
    return VoteSummaryResponse.builder()
        .id(vote.getId())
        .title(vote.getTitle())
        .selectionType(vote.getSelectionType())
        .voteType(vote.getVoteType())
        .startDate(vote.getStartDate())
        .endDate(vote.getEndDate())
        .voted(voted)
        .build();
  }
}
