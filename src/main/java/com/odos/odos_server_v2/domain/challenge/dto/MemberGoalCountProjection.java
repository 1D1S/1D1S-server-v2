package com.odos.odos_server_v2.domain.challenge.dto;

/** 챌린지 랭킹용: 멤버별 완료 목표 수 집계 결과. */
public interface MemberGoalCountProjection {
  Long getMemberId();

  long getCompletedGoalCount();
}
