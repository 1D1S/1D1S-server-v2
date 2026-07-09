package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 일정 기간 활동(일지 작성)이 없는 진행중 챌린지를 자동 종료 처리한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeAutoCloseService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final int INACTIVITY_DAYS = 14;

  private final ChallengeRepository challengeRepository;

  @Transactional
  public void closeInactiveChallenges() {
    LocalDate today = LocalDate.now(KST);
    LocalDate cutoffDate = today.minusDays(INACTIVITY_DAYS);
    LocalDateTime cutoff = cutoffDate.atStartOfDay();

    List<Long> targetIds =
        challengeRepository.findInactiveOngoingChallengeIds(
            today, cutoff, cutoffDate, ChallengeType.OFFICIAL);

    if (targetIds.isEmpty()) {
      log.info("비활성 챌린지 자동 종료: 대상 없음 (기준일 {} KST)", cutoffDate);
      return;
    }

    int closed = challengeRepository.closeChallengesByIds(targetIds, today);
    log.info("비활성 챌린지 자동 종료: {}건 종료 (기준일 {} KST, ids={})", closed, cutoffDate, targetIds);
  }
}
