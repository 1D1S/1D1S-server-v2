package com.odos.odos_server_v2.domain.member.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

/** 통계 집계 단위. 엔드포인트별 허용 값은 서비스에서 검증한다. */
@Schema(description = "통계 집계 단위")
public enum StatUnit {
  @Schema(description = "일 단위")
  DAY,
  @Schema(description = "주 단위(월요일 시작, KST)")
  WEEK,
  @Schema(description = "월 단위")
  MONTH,
  @Schema(description = "연 단위")
  YEAR
}
