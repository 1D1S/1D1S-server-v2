package com.odos.odos_server_v2.domain.member.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.friend.repository.FriendRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodSummaryResponse;
import com.odos.odos_server_v2.domain.member.statistics.repository.StatisticsRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * 기간 요약(summary) 파싱/경계 계산 회귀 테스트. 집계는 리포지토리 mock 으로 대체하여 순수 파싱·날짜계산·조립 경로만 검증한다. (DB 불필요) 회원 가입일은
 * 과거로 고정하므로 실행 시점(오늘)이 어떤 날짜여도 안정적으로 통과한다.
 */
class StatisticsServiceSummaryTest {

  private final StatisticsRepository statisticsRepository = mock(StatisticsRepository.class);
  private final MemberRepository memberRepository = mock(MemberRepository.class);
  private final FriendRepository friendRepository = mock(FriendRepository.class);
  private final StatisticsService service =
      new StatisticsService(statisticsRepository, memberRepository, friendRepository);

  @BeforeEach
  void stubEmptyAggregations() {
    Member member = mock(Member.class);
    when(member.getCreatedAt()).thenReturn(LocalDateTime.of(2020, 1, 1, 0, 0));
    when(memberRepository.findById(any())).thenReturn(Optional.of(member));
    lenient()
        .when(statisticsRepository.aggregateDailyCounts(any(), any(), any()))
        .thenReturn(List.of());
    lenient()
        .when(statisticsRepository.aggregateFeelings(any(), any(), any(), any()))
        .thenReturn(List.of());
    lenient().when(statisticsRepository.countCompletedGoals(any(), any(), any())).thenReturn(0L);
    lenient().when(statisticsRepository.countTotalGoals(any(), any(), any())).thenReturn(0L);
  }

  @Test
  void week_periodKey_isParsedToMondaySundayBounds() {
    PeriodSummaryResponse r = service.getSummary(1L, StatUnit.WEEK, "2026-W28");

    assertThat(r.start().toString()).isEqualTo("2026-07-06"); // 월요일
    assertThat(r.end().toString()).isEqualTo("2026-07-12"); // 일요일
    assertThat(r.subTrend()).hasSize(7); // 주간 하위 버킷 = 일 단위 7개
  }

  @Test
  void month_periodKey_isParsedToMonthBounds() {
    PeriodSummaryResponse r = service.getSummary(1L, StatUnit.MONTH, "2026-07");

    assertThat(r.start().toString()).isEqualTo("2026-07-01");
    assertThat(r.end().toString()).isEqualTo("2026-07-31");
    assertThat(r.subTrend()).hasSize(31);
  }

  @Test
  void year_periodKey_isParsedToYearBounds() {
    PeriodSummaryResponse r = service.getSummary(1L, StatUnit.YEAR, "2026");

    assertThat(r.start().toString()).isEqualTo("2026-01-01");
    assertThat(r.end().toString()).isEqualTo("2026-12-31");
    assertThat(r.subTrend()).hasSize(12); // 연간 하위 버킷 = 월 단위 12개
  }

  @Test
  void currentWeek_withNoDiaries_returnsZeroesWithoutThrowing() {
    assertThatCode(() -> service.getSummary(1L, StatUnit.WEEK, "2026-W28"))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"2026-W99", "2026-W00", "2025-W53", "2026-w28", "2026-28", "2026-W", "W28", "xx"})
  void malformedWeekKey_throws400NotServerError(String badKey) {
    assertThatThrownBy(() -> service.getSummary(1L, StatUnit.WEEK, badKey))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_STATISTICS_PERIOD);
  }
}
