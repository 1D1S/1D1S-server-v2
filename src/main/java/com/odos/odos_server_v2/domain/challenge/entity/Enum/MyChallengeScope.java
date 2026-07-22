package com.odos.odos_server_v2.domain.challenge.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "내 챌린지 조회 범위 "
            + "(ALL: 참여 이력 전체 - HOST/PARTICIPANT/LEAVE, 기간 무관 / "
            + "ONGOING: 현재 진행 중 참여 - HOST·PARTICIPANT & 오늘이 게시기간 내 / "
            + "ENDED: 그 외 - 종료됐거나 과거참여(LEAVE))")
public enum MyChallengeScope {
  ALL,
  ONGOING,
  ENDED
}
