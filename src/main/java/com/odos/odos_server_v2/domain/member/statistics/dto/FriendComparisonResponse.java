package com.odos.odos_server_v2.domain.member.statistics.dto;

import com.odos.odos_server_v2.domain.member.statistics.StatUnit;

/** 특정 친구와의 1:1 비교 응답. 같은 기간(period)의 나 vs 친구 지표를 함께 제공한다. */
public record FriendComparisonResponse(StatUnit period, MemberStats me, FriendStats friend) {

  /** 나의 지표. */
  public record MemberStats(long diaryCount, long completedGoalCount) {}

  /** 비교 대상 친구의 식별 정보 + 지표. */
  public record FriendStats(
      long memberId,
      String nickname,
      String profileUrl,
      long diaryCount,
      long completedGoalCount) {}
}
