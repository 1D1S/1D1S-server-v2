package com.odos.odos_server_v2.domain.diary.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.*;
import org.junit.jupiter.api.Test;

class DiaryClockTest {

  @Test
  void koreaMidnightIsPreviousDateInUtc() {
    LocalDate koreaDate = LocalDate.of(2026, 6, 10);

    ZonedDateTime koreaMidnight = koreaDate.atStartOfDay(ZoneId.of("Asia/Seoul"));
    LocalDate utcDate = koreaMidnight.withZoneSameInstant(ZoneOffset.UTC).toLocalDate();

    assertThat(utcDate).isEqualTo(LocalDate.of(2026, 6, 9));
    Instant now = Instant.now();
    System.out.println(now);
    System.out.println("새벽 시간 확인");
    System.out.println(LocalDateTime.of(2026, 6, 10, 3, 10).toLocalDate());
    ZonedDateTime nows = ZonedDateTime.now();
    System.out.println(nows);
    System.out.println(nows.getZone());
    System.out.println(nows.getOffset());
    // LocalDate
  }

  @Test
  void checkLocalDateNowTimezone() {
    System.out.println("system zone = " + ZoneId.systemDefault());

    System.out.println("LocalDate.now() = " + LocalDate.now());
    System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
    System.out.println("UTC date = " + LocalDate.now(ZoneOffset.UTC));
    System.out.println("UTC date = " + LocalDateTime.now(ZoneOffset.UTC));
    System.out.println("Seoul date = " + LocalDate.now(ZoneId.of("Asia/Seoul")));
  }
}
