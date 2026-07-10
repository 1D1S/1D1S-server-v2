package com.odos.odos_server_v2.domain.member.statistics.dto;

import com.odos.odos_server_v2.domain.member.statistics.StatUnit;

/** 친구 대비 비교 응답. 프라이버시상 개별 친구 값은 노출하지 않고 평균/순위만 제공한다. */
public record FriendComparisonResponse(
    StatUnit period,
    long friendCount,
    MemberStats me,
    AverageStats friendsAverage,
    RankStats myRank) {

  public record MemberStats(long diaryCount, long completedGoalCount) {}

  /** 친구들의 평균(본인 제외). 친구 0명이면 0. */
  public record AverageStats(double diaryCount, double completedGoalCount) {}

  /** byDiaryCount: 일지 수 기준 내 순위(1=최상위), outOf: 본인 포함 인원 수. */
  public record RankStats(int byDiaryCount, int outOf) {}
}
