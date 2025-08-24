package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChallengeRequest {
  private String title;
  private Category category;
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;
  private int maxParticipantCnt;
  private ChallengeType challengeType;
  private List<String> goals;
}
