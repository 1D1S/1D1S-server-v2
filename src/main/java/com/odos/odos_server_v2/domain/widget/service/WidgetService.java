package com.odos.odos_server_v2.domain.widget.service;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.service.MemberService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.domain.widget.dto.WidgetSummaryResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 앱 위젯 전용 조회. 위젯은 주기적으로 호출되므로 왕복과 쿼리 수를 최소화한다.
 *
 * <p>총 3쿼리(참여 중인 챌린지가 없으면 2쿼리):
 *
 * <ol>
 *   <li>일지 작성 날짜 목록 — 스트릭과 오늘 작성 여부를 함께 계산(MemberService 재사용)
 *   <li>진행중 + 승인 참여(HOST/PARTICIPANT) 챌린지 — 기존 홈 '오늘의 기록' 쿼리 재사용
 *   <li>그 챌린지들 중 오늘 일지가 있는 challengeId — IN 절 한 번(N+1 없음)
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetService {

  // 오늘 판정 기준 시간대. 스트릭 계산(MemberService)과 동일하게 KST 로 통일한다.
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  // 위젯은 좁은 화면에 몇 건만 노출하므로 상한을 둬 응답 크기를 고정한다.
  private static final int MAX_TODAY_CHALLENGES = 10;

  private static final List<ParticipantStatus> APPROVED_STATUSES =
      List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT);

  private final ParticipantRepository participantRepository;
  private final DiaryRepository diaryRepository;
  private final MemberService memberService;
  private final ImageService imageService;

  public WidgetSummaryResponse getSummary(Long memberId) {
    LocalDate today = LocalDate.now(KST);

    MemberService.WidgetStreak streak = memberService.getWidgetStreak(memberId);

    return new WidgetSummaryResponse(
        new WidgetSummaryResponse.StreakInfo(streak.currentStreak(), streak.todayWritten()),
        findTodayChallenges(memberId, today));
  }

  private List<WidgetSummaryResponse.TodayChallenge> findTodayChallenges(
      Long memberId, LocalDate today) {
    List<Participant> participants =
        participantRepository.findInProgressWithGoals(memberId, APPROVED_STATUSES, today);

    // 챌린지당 1건으로 정리(가장 먼저 참여한 행 유지). 홈 '오늘의 기록'과 동일한 방어 로직.
    Map<Long, Challenge> byChallenge = new LinkedHashMap<>();
    for (Participant p : participants) {
      byChallenge.putIfAbsent(p.getChallenge().getId(), p.getChallenge());
    }
    if (byChallenge.isEmpty()) {
      return List.of();
    }

    Set<Long> writtenToday =
        new HashSet<>(
            diaryRepository.findChallengeIdsWithDiaryOnDate(memberId, byChallenge.keySet(), today));

    // 마감 임박순(endDate asc, 무기한/미설정은 뒤로) → 동률이면 challengeId asc 로 안정 정렬.
    return byChallenge.values().stream()
        .filter(c -> !writtenToday.contains(c.getId()))
        .sorted(
            Comparator.comparing(
                    Challenge::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Challenge::getId))
        .limit(MAX_TODAY_CHALLENGES)
        .map(
            c ->
                new WidgetSummaryResponse.TodayChallenge(
                    c.getId(), c.getTitle(), imageService.getFileUrl(c.getThumbnailImage())))
        .toList();
  }
}
