package com.odos.odos_server_v2.domain.member.statistics.dto;

import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import com.odos.odos_server_v2.domain.member.statistics.dto.FeelingDistributionResponse.FeelingSlice;
import java.time.LocalDate;
import java.util.List;

/** 기간 요약 응답. subTrend 는 하위 단위(주/월=일, 연=월) 연속 버킷, peakBucket 은 그중 최댓값. */
public record PeriodSummaryResponse(
    StatUnit unit,
    String periodKey,
    LocalDate start,
    LocalDate end,
    long diaryCount,
    long diaryCountDelta,
    long activeDays,
    long completedGoalCount,
    double goalCompletionRate,
    List<FeelingSlice> feelingBreakdown,
    int maxStreakInPeriod,
    PeakBucket peakBucket,
    List<SubTrendPoint> subTrend,
    boolean hasPrev,
    boolean hasNext) {

  /** 하위 단위 최댓값 버킷. count 0 이면 key 는 null. */
  public record PeakBucket(String key, long count) {}

  /** 하위 단위 버킷(주/월=YYYY-MM-DD, 연=YYYY-MM). */
  public record SubTrendPoint(String bucket, long count) {}
}
