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
public class VoteDetailResponse {
  private Long id;
  private String title;
  private VoteSelectionType selectionType;
  private VoteType voteType;
  private LocalDate startDate;
  private LocalDate endDate;
  private boolean voted;
  private List<VoteOptionResultResponse> options;

  public static VoteDetailResponse of(
      Vote vote, boolean voted, List<VoteOptionResultResponse> options) {
    return VoteDetailResponse.builder()
        .id(vote.getId())
        .title(vote.getTitle())
        .selectionType(vote.getSelectionType())
        .voteType(vote.getVoteType())
        .startDate(vote.getStartDate())
        .endDate(vote.getEndDate())
        .voted(voted)
        .options(options)
        .build();
  }
}
