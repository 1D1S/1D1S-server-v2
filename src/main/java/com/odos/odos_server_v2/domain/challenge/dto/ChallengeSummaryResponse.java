package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.dto.LikeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "챌린지 요약 응답")
@Builder
@AllArgsConstructor
@Getter
public class ChallengeSummaryResponse {

  @Schema(description = "챌린지 ID", example = "1")
  private Long challengeId;

  @Schema(description = "챌린지 제목", example = "30일 코딩 챌린지")
  private String title;

  @Schema(description = "챌린지 카테고리", example = "DEV")
  private Category category;

  @Schema(description = "챌린지 시작일", example = "2025-09-01")
  private LocalDate startDate;

  @Schema(description = "챌린지 종료일", example = "2025-09-30")
  private LocalDate endDate;

  @Schema(description = "최대 참여 인원", example = "10")
  private Long maxParticipantCnt;

  @Schema(description = "챌린지 유형 (FIXED: 고정형, FLEXIBLE: 유연형)", example = "FIXED")
  private ChallengeType challengeType;

  @Schema(description = "현재 참여 인원", example = "5")
  private Long participantCnt;

  @Schema(description = "좋아요 정보")
  private LikeDto likeInfo;
}
