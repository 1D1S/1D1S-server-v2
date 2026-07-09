package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "챌린지 참여자 응답")
@Builder
@AllArgsConstructor
@Getter
public class ParticipantResponse {

  @Schema(description = "회원 ID", example = "1")
  private Long memberId;

  @Schema(description = "참여자 ID", example = "10")
  private Long participantId;

  @Schema(description = "닉네임", example = "홍길동")
  private String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImg;

  @Schema(
      description =
          "참여 상태 (PENDING: 대기, PARTICIPANT: 참여, HOST: 호스트, REJECTED: 거절, LEAVE: 탈퇴, NONE: 미참여)",
      example = "PARTICIPANT")
  private ParticipantStatus status;

  @Schema(description = "참여자의 챌린지 목표 목록")
  private List<ChallengeGoalDto> goals;

  @Schema(
      description = "챌린지 내 등수(스트릭 desc, 완료 목표 수 desc / 동점은 같은 등수). 랭킹이 계산되는 응답에서만 채워지고 그 외에는 null.",
      example = "1")
  private Integer rank;

  @Schema(description = "챌린지 내 최장 스트릭(연속 일지 작성일). 랭킹 응답에서만 채워짐.", example = "5")
  private Integer streak;

  @Schema(description = "챌린지 내 완료한 목표 수. 랭킹 응답에서만 채워짐.", example = "12")
  private Integer completedGoalCount;
}
