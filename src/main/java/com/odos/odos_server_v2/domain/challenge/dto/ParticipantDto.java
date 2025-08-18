package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ParticipantDto {
  private Long memberId;
  private Long participantId;
  private String nickname;
  private String profileImg;
  private ParticipantStatus status;
}
