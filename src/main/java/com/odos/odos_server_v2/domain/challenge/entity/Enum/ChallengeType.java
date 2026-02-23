package com.odos.odos_server_v2.domain.challenge.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 유형 (FIXED: 호스트가 목표를 설정하고 모든 참여자가 동일한 목표를 수행, FLEXIBLE: 참여자가 각자 목표를 설정)")
public enum ChallengeType {
  FIXED,
  FLEXIBLE
}
