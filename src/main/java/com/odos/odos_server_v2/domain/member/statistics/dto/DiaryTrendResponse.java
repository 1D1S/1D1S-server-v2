package com.odos.odos_server_v2.domain.member.statistics.dto;

import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import java.time.LocalDate;
import java.util.List;

/** 기간별 일지 추이 응답. points 는 빈 버킷도 count 0 으로 채워 연속 구간을 반환한다. */
public record DiaryTrendResponse(
    StatUnit unit, LocalDate from, LocalDate to, List<TrendPoint> points) {

  /** bucket: 버킷 시작일(주=월요일, 월=1일), count: 해당 버킷 일지 수. */
  public record TrendPoint(LocalDate bucket, long count) {}
}
