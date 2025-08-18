package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.dto.LikeDto;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ChallengeSummaryResponse {
  private Long challengeId;
  private String title;
  private Category category;
  private LocalDate startDate;
  private LocalDate endDate;
  private Long maxParticipantCnt;
  private ChallengeType challengeType;
  private Long participantCnt;
  private LikeDto likeInfo;
}
