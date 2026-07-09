package com.odos.odos_server_v2.domain.challenge.entity.Enum;

/** 챌린지 참여자 목록 정렬 옵션. */
public enum ParticipantSortType {
  /** 참여순(참여자 id 오름차순). 기본값. */
  PARTICIPATION,
  /** 등수순(스트릭 desc, 완료 목표 수 desc). */
  RANK
}
