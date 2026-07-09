package com.odos.odos_server_v2.domain.challenge.dto;

import java.time.LocalDate;

/** 챌린지 랭킹용: 멤버별 일지 작성 날짜(스트릭 계산용) 행. */
public interface MemberDiaryDateProjection {
  Long getMemberId();

  LocalDate getCompletedDate();
}
