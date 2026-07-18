package com.odos.odos_server_v2.domain.vote.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.odos.odos_server_v2.domain.vote.entity.Vote;
import com.odos.odos_server_v2.domain.vote.entity.VoteSelectionType;
import com.odos.odos_server_v2.domain.vote.entity.VoteType;

@Getter
@Builder
public class AdminVoteDetailResponse {
  private Long id;
  private String title;
  private VoteSelectionType selectionType;
  private VoteType voteType;
  private LocalDate startDate;
  private LocalDate endDate;
  private long participantCount;
  private List<AdminVoteOptionResponse> options;

  public static AdminVoteDetailResponse of(
      Vote vote, long participantCount, List<AdminVoteOptionResponse> options) {
    return AdminVoteDetailResponse.builder()
        .id(vote.getId())
        .title(vote.getTitle())
        .selectionType(vote.getSelectionType())
        .voteType(vote.getVoteType())
        .startDate(vote.getStartDate())
        .endDate(vote.getEndDate())
        .participantCount(participantCount)
        .options(options)
        .build();
  }
}
