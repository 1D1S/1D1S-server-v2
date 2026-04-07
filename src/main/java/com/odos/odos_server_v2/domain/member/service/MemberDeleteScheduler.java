package com.odos.odos_server_v2.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDeleteScheduler {
  private final MemberDeleteService memberDeleteService;

  @Scheduled(cron = "0 0 3 * * *")
  public void deleteWithdrawnMembers() {
    memberDeleteService.processDeletion();
  }
}
