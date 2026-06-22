package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "챌린지 미리보기 응답")
@AllArgsConstructor
@Getter
public class ChallengePreviewResponse {

  @Schema(description = "챌린지 제목", example = "30일 코딩 챌린지")
  private String title;

  @Schema(description = "챌린지 유형 (FIXED: 고정목표, FLEXIBLE: 자유목표)", example = "FLEXIBLE")
  private GoalType goalType;

  @Schema(description = "챌린지 형태 (INDIVIDUAL: 개인, GROUP: 단체)", example = "GROUP")
  private ParticipationType participationType;

  @Schema(description = "챌린지 종류 (PUBLIC: 공개, PRIVATE: 비공개, OFFICIAL: 공식)", example = "PRIVATE")
  private ChallengeType challengeType;
}
