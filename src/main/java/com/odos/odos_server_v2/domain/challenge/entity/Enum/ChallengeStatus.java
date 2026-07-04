package com.odos.odos_server_v2.domain.challenge.entity.Enum;

/** 챌린지 진행 상태 필터 값. 별도 컬럼이 아니라 startDate/endDate와 오늘 날짜로 판정한다. */
public enum ChallengeStatus {
  UPCOMING, // 모집중 (시작 전)
  ONGOING, // 진행중
  ENDED // 종료
}
