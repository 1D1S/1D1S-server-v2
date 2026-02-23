package com.odos.odos_server_v2.domain.challenge.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "참여 상태 (PENDING: 신청 대기, PARTICIPANT: 참여 승인, HOST: 호스트, REJECTED: 신청 거절, LEAVE: 탈퇴, NONE: 미신청)")
public enum ParticipantStatus {
  PENDING,
  REJECTED,
  PARTICIPANT,
  HOST,
  LEAVE,
  NONE
}
