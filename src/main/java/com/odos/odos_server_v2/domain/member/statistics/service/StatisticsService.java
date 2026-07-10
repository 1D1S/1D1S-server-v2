package com.odos.odos_server_v2.domain.member.statistics.service;

import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import com.odos.odos_server_v2.domain.friend.repository.FriendRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import com.odos.odos_server_v2.domain.member.statistics.dto.DiaryTrendResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.DiaryTrendResponse.TrendPoint;
import com.odos.odos_server_v2.domain.member.statistics.dto.FeelingDistributionResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.FeelingDistributionResponse.FeelingSlice;
import com.odos.odos_server_v2.domain.member.statistics.dto.FriendComparisonResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.FriendComparisonResponse.AverageStats;
import com.odos.odos_server_v2.domain.member.statistics.dto.FriendComparisonResponse.MemberStats;
import com.odos.odos_server_v2.domain.member.statistics.dto.FriendComparisonResponse.RankStats;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodListResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodListResponse.PeriodItem;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodSummaryResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodSummaryResponse.PeakBucket;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodSummaryResponse.SubTrendPoint;
import com.odos.odos_server_v2.domain.member.statistics.repository.StatisticsRepository;
import com.odos.odos_server_v2.domain.member.statistics.repository.StatisticsRepository.DailyCount;
import com.odos.odos_server_v2.domain.member.statistics.repository.StatisticsRepository.FeelingCount;
import com.odos.odos_server_v2.domain.member.statistics.repository.StatisticsRepository.MemberCount;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 통계 API 서비스. 모든 집계는 {@link StatisticsRepository} 의 DB GROUP BY/COUNT 로 수행한다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final WeekFields ISO_WEEK = WeekFields.ISO;

  private final StatisticsRepository statisticsRepository;
  private final MemberRepository memberRepository;
  private final FriendRepository friendRepository;

  // ---------------------------------------------------------------------------
  // 1. 감정 분포
  // ---------------------------------------------------------------------------
  public FeelingDistributionResponse getFeelingDistribution(
      Long memberId, LocalDate from, LocalDate to, Long challengeId) {
    if (from != null && to != null && from.isAfter(to)) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD);
    }
    List<FeelingCount> rows =
        statisticsRepository.aggregateFeelings(memberId, from, to, challengeId);
    long total = rows.stream().mapToLong(FeelingCount::getCount).sum();
    return new FeelingDistributionResponse(total, buildFeelingSlices(rows, total));
  }

  // ---------------------------------------------------------------------------
  // 2. 기간별 추이
  // ---------------------------------------------------------------------------
  public DiaryTrendResponse getDiaryTrend(
      Long memberId, StatUnit unit, LocalDate from, LocalDate to) {
    if (unit == StatUnit.YEAR) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD); // 추이는 DAY/WEEK/MONTH 만
    }
    LocalDate signupDate = signupDate(memberId);
    LocalDate today = today();

    LocalDate effectiveTo = (to != null) ? to : today;
    if (effectiveTo.isAfter(today) || effectiveTo.isBefore(signupDate)) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD); // 미래/가입 이전
    }
    LocalDate effectiveFrom = (from != null) ? from : defaultTrendFrom(unit, effectiveTo);
    if (effectiveFrom.isAfter(effectiveTo)) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD);
    }
    // 탐색 하한 = 가입일로 clamp
    if (effectiveFrom.isBefore(signupDate)) {
      effectiveFrom = signupDate;
    }

    Map<LocalDate, Long> daily = dailyCountMap(memberId, effectiveFrom, effectiveTo);

    // 버킷 시작일 기준으로 합산 후 빈 버킷 0 채우기
    Map<LocalDate, Long> byBucket = new LinkedHashMap<>();
    for (LocalDate b = bucketStart(unit, effectiveFrom);
        !b.isAfter(effectiveTo);
        b = nextBucket(unit, b)) {
      byBucket.put(b, 0L);
    }
    daily.forEach((date, count) -> byBucket.merge(bucketStart(unit, date), count, Long::sum));

    List<TrendPoint> points =
        byBucket.entrySet().stream().map(e -> new TrendPoint(e.getKey(), e.getValue())).toList();
    return new DiaryTrendResponse(unit, effectiveFrom, effectiveTo, points);
  }

  // ---------------------------------------------------------------------------
  // 3. 기간 목록
  // ---------------------------------------------------------------------------
  public PeriodListResponse getPeriods(Long memberId, StatUnit unit) {
    requirePeriodUnit(unit);
    LocalDate signupDate = signupDate(memberId);
    LocalDate today = today();

    List<PeriodItem> periods = new ArrayList<>();
    LocalDate cursor = bucketStart(unit, signupDate);
    LocalDate currentStart = bucketStart(unit, today);
    while (!cursor.isAfter(currentStart)) {
      LocalDate end = bucketEnd(unit, cursor);
      periods.add(new PeriodItem(periodKey(unit, cursor), cursor, end, periodLabel(unit, cursor)));
      cursor = nextBucket(unit, cursor);
    }
    return new PeriodListResponse(unit, signupDate, periods);
  }

  // ---------------------------------------------------------------------------
  // 4. 기간 요약
  // ---------------------------------------------------------------------------
  public PeriodSummaryResponse getSummary(Long memberId, StatUnit unit, String periodKey) {
    requirePeriodUnit(unit);
    LocalDate signupDate = signupDate(memberId);
    LocalDate today = today();

    LocalDate start = parsePeriodKey(unit, periodKey);
    LocalDate end = bucketEnd(unit, start);
    if (end.isBefore(signupDate) || start.isAfter(today)) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD); // 가입 이전/미래
    }

    Map<LocalDate, Long> daily = dailyCountMap(memberId, start, end);
    long diaryCount = daily.values().stream().mapToLong(Long::longValue).sum();
    long activeDays = daily.keySet().stream().filter(d -> daily.get(d) > 0).count();
    int maxStreak = maxConsecutive(new ArrayList<>(daily.keySet()));

    // 직전 동일 단위 대비 증감
    LocalDate prevStart = previousBucket(unit, start);
    LocalDate prevEnd = bucketEnd(unit, prevStart);
    long prevDiaryCount =
        dailyCountMap(memberId, prevStart, prevEnd).values().stream()
            .mapToLong(Long::longValue)
            .sum();

    long completedGoals = statisticsRepository.countCompletedGoals(memberId, start, end);
    long totalGoals = statisticsRepository.countTotalGoals(memberId, start, end);
    double completionRate = (totalGoals == 0) ? 0.0 : round4((double) completedGoals / totalGoals);

    List<FeelingCount> feelingRows =
        statisticsRepository.aggregateFeelings(memberId, start, end, null);
    long feelingTotal = feelingRows.stream().mapToLong(FeelingCount::getCount).sum();
    List<FeelingSlice> feelingBreakdown = buildFeelingSlices(feelingRows, feelingTotal);

    // 하위 버킷: 주/월 -> 일, 연 -> 월
    StatUnit subUnit = (unit == StatUnit.YEAR) ? StatUnit.MONTH : StatUnit.DAY;
    List<SubTrendPoint> subTrend = buildSubTrend(subUnit, start, end, daily);
    PeakBucket peak =
        subTrend.stream()
            .max((a, b) -> Long.compare(a.count(), b.count()))
            .filter(p -> p.count() > 0)
            .map(p -> new PeakBucket(p.bucket(), p.count()))
            .orElse(new PeakBucket(null, 0));

    boolean hasPrev = start.isAfter(bucketStart(unit, signupDate));
    boolean hasNext = start.isBefore(bucketStart(unit, today));

    return new PeriodSummaryResponse(
        unit,
        periodKey,
        start,
        end,
        diaryCount,
        diaryCount - prevDiaryCount,
        activeDays,
        completedGoals,
        completionRate,
        feelingBreakdown,
        maxStreak,
        peak,
        subTrend,
        hasPrev,
        hasNext);
  }

  // ---------------------------------------------------------------------------
  // 5. 친구 대비 비교
  // ---------------------------------------------------------------------------
  public FriendComparisonResponse getFriendComparison(Long memberId, StatUnit period) {
    if (period != StatUnit.WEEK && period != StatUnit.MONTH) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD); // 비교는 WEEK/MONTH 만
    }
    Member me =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    LocalDate today = today();
    LocalDate start = bucketStart(period, today);
    LocalDate end = bucketEnd(period, today);

    // 친구 id 는 findByMember 한 번으로 (friendMember 는 LAZY 지만 id 접근은 프록시로 추가 쿼리 없음)
    List<Long> friendIds =
        friendRepository.findByMember(me).stream()
            .map(f -> f.getFriendMember().getId())
            .distinct()
            .toList();

    List<Long> memberIds = new ArrayList<>(friendIds);
    memberIds.add(memberId);

    Map<Long, Long> diaryCounts =
        toMemberCountMap(statisticsRepository.countDiariesByMembers(memberIds, start, end));
    Map<Long, Long> goalCounts =
        toMemberCountMap(statisticsRepository.countCompletedGoalsByMembers(memberIds, start, end));

    long myDiary = diaryCounts.getOrDefault(memberId, 0L);
    long myGoal = goalCounts.getOrDefault(memberId, 0L);

    double avgDiary =
        friendIds.isEmpty()
            ? 0.0
            : round4(
                friendIds.stream().mapToLong(id -> diaryCounts.getOrDefault(id, 0L)).sum()
                    / (double) friendIds.size());
    double avgGoal =
        friendIds.isEmpty()
            ? 0.0
            : round4(
                friendIds.stream().mapToLong(id -> goalCounts.getOrDefault(id, 0L)).sum()
                    / (double) friendIds.size());

    // 순위: 나보다 일지 수가 많은 인원 수 + 1 (본인 포함 집단 기준, 동점은 같은 순위)
    int rank =
        1
            + (int)
                memberIds.stream().filter(id -> diaryCounts.getOrDefault(id, 0L) > myDiary).count();

    return new FriendComparisonResponse(
        period,
        friendIds.size(),
        new MemberStats(myDiary, myGoal),
        new AverageStats(avgDiary, avgGoal),
        new RankStats(rank, memberIds.size()));
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------
  private LocalDate today() {
    return LocalDate.now(KST);
  }

  private LocalDate signupDate(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return member.getCreatedAt().toLocalDate();
  }

  private Map<LocalDate, Long> dailyCountMap(Long memberId, LocalDate from, LocalDate to) {
    return statisticsRepository.aggregateDailyCounts(memberId, from, to).stream()
        .collect(Collectors.toMap(DailyCount::getBucket, DailyCount::getCount));
  }

  private Map<Long, Long> toMemberCountMap(List<MemberCount> rows) {
    Map<Long, Long> map = new LinkedHashMap<>();
    for (MemberCount r : rows) {
      map.put(r.getMemberId(), r.getCount());
    }
    return map;
  }

  private List<SubTrendPoint> buildSubTrend(
      StatUnit subUnit, LocalDate start, LocalDate end, Map<LocalDate, Long> daily) {
    Map<LocalDate, Long> byBucket = new LinkedHashMap<>();
    for (LocalDate b = start; !b.isAfter(end); b = nextBucket(subUnit, b)) {
      byBucket.put(b, 0L);
    }
    daily.forEach((date, count) -> byBucket.merge(bucketStart(subUnit, date), count, Long::sum));
    return byBucket.entrySet().stream()
        .map(e -> new SubTrendPoint(subBucketKey(subUnit, e.getKey()), e.getValue()))
        .toList();
  }

  private List<FeelingSlice> buildFeelingSlices(List<FeelingCount> rows, long total) {
    Map<String, Long> byName = new LinkedHashMap<>();
    long noneCount = 0;
    for (FeelingCount row : rows) {
      Feeling f = row.getFeeling();
      if (f == null || f == Feeling.NONE) {
        noneCount += row.getCount();
      } else {
        byName.merge(f.name(), row.getCount(), Long::sum);
      }
    }
    List<FeelingSlice> slices = new ArrayList<>();
    for (Feeling f : List.of(Feeling.HAPPY, Feeling.SAD, Feeling.NORMAL)) {
      Long c = byName.get(f.name());
      if (c != null) {
        slices.add(new FeelingSlice(f.name(), c, ratio(c, total)));
      }
    }
    if (noneCount > 0) {
      slices.add(new FeelingSlice(Feeling.NONE.name(), noneCount, ratio(noneCount, total)));
    }
    return slices;
  }

  private double ratio(long count, long total) {
    return (total == 0) ? 0.0 : round4((double) count / total);
  }

  private static double round4(double v) {
    return Math.round(v * 10000.0) / 10000.0;
  }

  private static int maxConsecutive(List<LocalDate> dates) {
    if (dates.isEmpty()) {
      return 0;
    }
    List<LocalDate> sorted = dates.stream().sorted().toList();
    int max = 1;
    int cur = 1;
    for (int i = 1; i < sorted.size(); i++) {
      if (sorted.get(i - 1).plusDays(1).equals(sorted.get(i))) {
        cur++;
      } else {
        max = Math.max(max, cur);
        cur = 1;
      }
    }
    return Math.max(max, cur);
  }

  private LocalDate defaultTrendFrom(StatUnit unit, LocalDate to) {
    return switch (unit) {
      case DAY -> to.minusDays(29); // 최근 30일
      case WEEK -> to.minusWeeks(11); // 최근 12주
      case MONTH -> to.minusMonths(11); // 최근 12개월
      case YEAR -> to; // 도달 불가(위에서 차단)
    };
  }

  private void requirePeriodUnit(StatUnit unit) {
    if (unit != StatUnit.WEEK && unit != StatUnit.MONTH && unit != StatUnit.YEAR) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD);
    }
  }

  // --- 버킷 경계 계산 -----------------------------------------------------------
  private LocalDate bucketStart(StatUnit unit, LocalDate date) {
    return switch (unit) {
      case DAY -> date;
      case WEEK -> date.with(ISO_WEEK.dayOfWeek(), 1); // 월요일
      case MONTH -> date.withDayOfMonth(1);
      case YEAR -> date.withDayOfYear(1);
    };
  }

  private LocalDate nextBucket(StatUnit unit, LocalDate start) {
    return switch (unit) {
      case DAY -> start.plusDays(1);
      case WEEK -> start.plusWeeks(1);
      case MONTH -> start.plusMonths(1);
      case YEAR -> start.plusYears(1);
    };
  }

  private LocalDate previousBucket(StatUnit unit, LocalDate start) {
    return switch (unit) {
      case DAY -> start.minusDays(1);
      case WEEK -> start.minusWeeks(1);
      case MONTH -> start.minusMonths(1);
      case YEAR -> start.minusYears(1);
    };
  }

  private LocalDate bucketEnd(StatUnit unit, LocalDate start) {
    return nextBucket(unit, start).minusDays(1);
  }

  // --- periodKey 포맷/파싱 ------------------------------------------------------
  private String periodKey(StatUnit unit, LocalDate start) {
    return switch (unit) {
      case WEEK ->
          String.format(
              "%04d-W%02d",
              start.get(ISO_WEEK.weekBasedYear()), start.get(ISO_WEEK.weekOfWeekBasedYear()));
      case MONTH -> String.format("%04d-%02d", start.getYear(), start.getMonthValue());
      case YEAR -> String.format("%04d", start.getYear());
      case DAY -> start.toString();
    };
  }

  private String periodLabel(StatUnit unit, LocalDate start) {
    return switch (unit) {
      case WEEK ->
          start.get(ISO_WEEK.weekBasedYear())
              + "년 "
              + start.get(ISO_WEEK.weekOfWeekBasedYear())
              + "주차";
      case MONTH -> start.getYear() + "년 " + start.getMonthValue() + "월";
      case YEAR -> start.getYear() + "년";
      case DAY -> start.toString();
    };
  }

  private String subBucketKey(StatUnit subUnit, LocalDate start) {
    return (subUnit == StatUnit.MONTH)
        ? String.format("%04d-%02d", start.getYear(), start.getMonthValue())
        : start.toString();
  }

  private LocalDate parsePeriodKey(StatUnit unit, String periodKey) {
    if (periodKey == null || periodKey.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD);
    }
    try {
      return switch (unit) {
        case WEEK -> {
          String[] parts = periodKey.split("-W");
          int weekYear = Integer.parseInt(parts[0]);
          int week = Integer.parseInt(parts[1]);
          LocalDate parsed =
              LocalDate.of(weekYear, 1, 4) // 1월 4일은 항상 ISO 1주차
                  .with(ISO_WEEK.weekOfWeekBasedYear(), week)
                  .with(ISO_WEEK.dayOfWeek(), 1);
          // 정규화 검증: 되돌린 key 가 요청과 일치해야 함(예: 2026-W99 차단)
          if (!periodKey(StatUnit.WEEK, parsed).equals(normalizeWeekKey(weekYear, week))) {
            throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD);
          }
          yield parsed;
        }
        case MONTH -> {
          String[] parts = periodKey.split("-");
          yield LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1);
        }
        case YEAR -> LocalDate.of(Integer.parseInt(periodKey), 1, 1);
        case DAY -> LocalDate.parse(periodKey);
      };
    } catch (CustomException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new CustomException(ErrorCode.INVALID_STATISTICS_PERIOD);
    }
  }

  private String normalizeWeekKey(int weekYear, int week) {
    return String.format("%04d-W%02d", weekYear, week);
  }
}
