package com.odos.odos_server_v2.domain.member.statistics.dto;

import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import java.time.LocalDate;
import java.util.List;

/** 통계 기간 목록 응답. 가입~현재를 연속 생성(DB 조회 없이 날짜 계산). */
public record PeriodListResponse(StatUnit unit, LocalDate signupDate, List<PeriodItem> periods) {

  /** key: 2026-W26 / 2026-06 / 2026, start/end: 포함 경계, label: 표시용 라벨. */
  public record PeriodItem(String key, LocalDate start, LocalDate end, String label) {}
}
