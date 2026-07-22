package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "내 챌린지(전체보기) 항목 응답 — 챌린지 요약 + 내 참여 상태")
@Builder
@AllArgsConstructor
@Getter
public class MyChallengeResponse {

  @Schema(
      description =
          "이 챌린지에 대한 내 참여 상태 (HOST: 호스트, PARTICIPANT: 참여중, LEAVE: 과거참여/탈퇴). " + "\"참여종료\" 구분에 사용.",
      example = "PARTICIPANT")
  private ParticipantStatus participationStatus;

  @Schema(description = "챌린지 요약 정보")
  private ChallengeSummaryResponse challenge;
}
