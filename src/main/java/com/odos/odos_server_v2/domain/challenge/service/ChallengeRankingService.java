package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.MemberDiaryDateProjection;
import com.odos.odos_server_v2.domain.challenge.dto.MemberGoalCountProjection;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 챌린지 참여자 랭킹 계산.
 *
 * <p>등수 기준: 챌린지 내 최장 스트릭(연속 일지 작성일, Diary.completedDate 기준) desc → 완료 목표 수(DiaryGoal.isCompleted)
 * desc. 동점(스트릭·목표 수 모두 동일)은 같은 등수를 부여하고 다음 등수는 건너뛴다(경쟁 순위, 1·2·2·4). 목록 정렬의 결정성을 위해 동점 내에서는
 * participantId 오름차순.
 *
 * <p>성능: 참여자 수만큼 반복 조회하지 않고, 완료 목표 수는 group by 집계 1회, 스트릭은 멤버별 일지 날짜를 1회에 모아 메모리에서 계산한다. 총 2 쿼리로 전체
 * 참여자의 지표를 구한다.
 */
@Service
@RequiredArgsConstructor
public class ChallengeRankingService {

  private final DiaryRepository diaryRepository;
  private final DiaryGoalRepository diaryGoalRepository;

  /** 참여자별 랭킹 지표. */
  public record RankInfo(int rank, int streak, long completedGoalCount) {}

  /**
   * 주어진 참여자 목록(가시성 필터가 이미 적용된 집합)에 대해 participantId → RankInfo 맵을 반환한다.
   *
   * @param challengeId 대상 챌린지
   * @param participants 등수를 매길 참여자 목록
   */
  public Map<Long, RankInfo> computeRanks(Long challengeId, List<Participant> participants) {
    // 1) 완료 목표 수 (group by 집계 1회)
    Map<Long, Long> goalCountByMember = new HashMap<>();
    for (MemberGoalCountProjection row :
        diaryGoalRepository.countCompletedGoalsByMemberForChallenge(challengeId)) {
      goalCountByMember.put(row.getMemberId(), row.getCompletedGoalCount());
    }

    // 2) 멤버별 일지 날짜 집합 (조회 1회 → 메모리에서 스트릭 계산)
    Map<Long, Set<LocalDate>> datesByMember = new HashMap<>();
    for (MemberDiaryDateProjection row :
        diaryRepository.findMemberDiaryDatesForChallenge(challengeId)) {
      if (row.getCompletedDate() == null) {
        continue;
      }
      datesByMember
          .computeIfAbsent(row.getMemberId(), k -> new HashSet<>())
          .add(row.getCompletedDate());
    }

    // 3) 참여자별 (스트릭, 완료 목표 수) 계산 후 정렬
    List<Entry> entries = new ArrayList<>();
    for (Participant participant : participants) {
      Long memberId = participant.getMember().getId();
      int streak = maxStreak(datesByMember.get(memberId));
      long goals = goalCountByMember.getOrDefault(memberId, 0L);
      entries.add(new Entry(participant.getId(), streak, goals));
    }
    entries.sort(
        Comparator.comparingInt((Entry e) -> e.streak)
            .reversed() // 스트릭 desc
            .thenComparing(Comparator.comparingLong((Entry e) -> e.goals).reversed()) // 완료 목표 수 desc
            .thenComparing(e -> e.participantId)); // 동점 내 결정성: participantId asc

    // 4) 경쟁 순위 부여(동점 같은 등수, 다음 등수 건너뛰기)
    Map<Long, RankInfo> result = new HashMap<>();
    int rank = 0;
    int position = 0;
    Integer prevStreak = null;
    Long prevGoals = null;
    for (Entry e : entries) {
      position++;
      if (prevStreak == null || e.streak != prevStreak || e.goals != prevGoals) {
        rank = position;
        prevStreak = e.streak;
        prevGoals = e.goals;
      }
      result.put(e.participantId, new RankInfo(rank, e.streak, e.goals));
    }
    return result;
  }

  /** MemberService.calculateMaxStreak 와 동일한 정의: 연속된 날짜의 최장 길이. */
  private int maxStreak(Set<LocalDate> dates) {
    if (dates == null || dates.isEmpty()) {
      return 0;
    }
    List<LocalDate> sorted = dates.stream().sorted().toList();
    int max = 1;
    int temp = 1;
    for (int i = 1; i < sorted.size(); i++) {
      if (sorted.get(i - 1).plusDays(1).equals(sorted.get(i))) {
        temp++;
      } else {
        max = Math.max(max, temp);
        temp = 1;
      }
    }
    return Math.max(max, temp);
  }

  private static final class Entry {
    private final Long participantId;
    private final int streak;
    private final long goals;

    private Entry(Long participantId, int streak, long goals) {
      this.participantId = participantId;
      this.streak = streak;
      this.goals = goals;
    }
  }
}
