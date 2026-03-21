package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "챌린지 상세 정보")
@Builder
@AllArgsConstructor
@Getter
public class ChallengeDetailDto {

  @Schema(description = "챌린지 설명", example = "매일 1시간씩 코딩 공부를 진행합니다.")
  private String description;

  @Schema(
      description =
          "현재 로그인한 사용자의 참여 상태 (PENDING: 대기, PARTICIPANT: 참여, HOST: 호스트, REJECTED: 거절, LEAVE: 탈퇴, NONE: 미참여)",
      example = "NONE")
  private ParticipantStatus myStatus;

  @Schema(description = "참여율 (현재 참여 인원 / 최대 참여 인원)", example = "0.5")
  private double participationRate;

  @Schema(description = "목표 달성률", example = "0.75")
  private double goalCompletionRate;
}
