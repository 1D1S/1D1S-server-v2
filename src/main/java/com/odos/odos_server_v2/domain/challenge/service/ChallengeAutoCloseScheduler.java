package com.odos.odos_server_v2.domain.challenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeAutoCloseScheduler {

  private final ChallengeAutoCloseService challengeAutoCloseService;

  /** 매일 04:00 KST 실행. 14일간 활동 없는 진행중 챌린지를 종료 처리한다. */
  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  public void closeInactiveChallenges() {
    challengeAutoCloseService.closeInactiveChallenges();
  }
}
