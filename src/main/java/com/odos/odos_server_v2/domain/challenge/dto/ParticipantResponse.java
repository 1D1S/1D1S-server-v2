package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ParticipantResponse {
  private Long memberId;
  private Long participantId;
  private String nickname;
  private String profileImg;
  private ParticipantStatus status;
}
