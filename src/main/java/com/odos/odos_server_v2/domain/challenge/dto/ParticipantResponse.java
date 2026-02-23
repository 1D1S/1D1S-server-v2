package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
}
