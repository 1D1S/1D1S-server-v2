package com.odos.odos_server_v2.domain.member.statistics.dto;

import java.util.List;

/** 감정 분포 응답. null/NONE 은 미선택(NONE)으로 병합되어 포함된다. */
public record FeelingDistributionResponse(long total, List<FeelingSlice> distribution) {

  /** feeling: HAPPY/SAD/NORMAL/NONE(미선택), ratio: total 대비 비율(0~1, total 0 이면 0). */
  public record FeelingSlice(String feeling, long count, double ratio) {}
}
