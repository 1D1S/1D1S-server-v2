package com.odos.odos_server_v2.domain.member.statistics.controller;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import com.odos.odos_server_v2.domain.member.statistics.dto.DiaryTrendResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.FeelingDistributionResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.FriendComparisonResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodListResponse;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodSummaryResponse;
import com.odos.odos_server_v2.domain.member.statistics.service.StatisticsService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 통계", description = "로그인 회원의 일지/목표/감정 통계 API (온디맨드 조회)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member/statistics")
public class StatisticsController {

  private final StatisticsService statisticsService;

  @Operation(
      summary = "감정 분포",
      description =
          "기간(from~to, 선택)과 챌린지(challengeId, 선택)로 필터링한 감정별 일지 수/비율. "
              + "감정 미선택(null/NONE)은 NONE 항목으로 병합되어 포함된다.")
  @GetMapping("/feelings")
  public ApiResponse<FeelingDistributionResponse> getFeelings(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_STAT_FEELINGS,
        statisticsService.getFeelingDistribution(memberId, from, to, challengeId));
  }

  @Operation(
      summary = "기간별 일지 추이",
      description =
          "unit(DAY/WEEK/MONTH) 버킷별 일지 수. from/to 미지정 시 unit별 최근 범위(일=30일, 주=12주, 월=12개월). "
              + "빈 버킷은 count 0 으로 채워 반환한다.")
  @GetMapping("/diary-trend")
  public ApiResponse<DiaryTrendResponse> getDiaryTrend(
      @RequestParam(defaultValue = "DAY") StatUnit unit,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_STAT_DIARY_TREND, statisticsService.getDiaryTrend(memberId, unit, from, to));
  }

  @Operation(
      summary = "통계 기간 목록",
      description = "unit(WEEK/MONTH/YEAR) 기준 가입일~현재의 연속 기간 목록. DB 조회 없이 날짜 계산으로 생성한다.")
  @GetMapping("/periods")
  public ApiResponse<PeriodListResponse> getPeriods(@RequestParam StatUnit unit) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_STAT_PERIODS, statisticsService.getPeriods(memberId, unit));
  }

  @Operation(
      summary = "기간 요약",
      description =
          "unit(WEEK/MONTH/YEAR)과 periodKey(예: 2026-W26 / 2026-06 / 2026)의 요약 지표. "
              + "가입 이전/미래 기간이면 400(STAT-001).")
  @GetMapping("/summary")
  public ApiResponse<PeriodSummaryResponse> getSummary(
      @RequestParam StatUnit unit, @RequestParam String periodKey) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_STAT_SUMMARY, statisticsService.getSummary(memberId, unit, periodKey));
  }

  @Operation(
      summary = "특정 친구와 1:1 비교",
      description =
          "period(WEEK/MONTH, 기본 MONTH) 기간의 나 vs 특정 친구(friendId) 지표(일지 수/완료 목표 수) 비교. "
              + "friendId 는 실제 내 친구여야 하며(아니면 FRIEND-007), 미지정 시 400. 친구 닉네임/프로필 URL 포함.")
  @GetMapping("/friend-comparison")
  public ApiResponse<FriendComparisonResponse> getFriendComparison(
      @RequestParam(defaultValue = "MONTH") StatUnit period, @RequestParam Long friendId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_STAT_FRIEND_COMPARISON,
        statisticsService.getFriendComparison(memberId, period, friendId));
  }
}
