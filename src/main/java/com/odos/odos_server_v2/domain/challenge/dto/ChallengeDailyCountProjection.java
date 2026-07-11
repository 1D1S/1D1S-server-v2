package com.odos.odos_server_v2.domain.challenge.dto;

import java.time.LocalDate;

/** 챌린지 통계용: 날짜별 일지 개수(일지 추이) 행. */
public interface ChallengeDailyCountProjection {
  LocalDate getBucket();

  long getCnt();
}
