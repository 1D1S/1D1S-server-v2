package com.odos.odos_server_v2.domain.member.statistics.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 통계 API 전용 집계 리포지토리. 모든 쿼리는 DB 에서 GROUP BY/COUNT 로 집계하며, 앱으로 일지 전체를 로드하지 않는다. (기존 MemberService 의
 * 전체 로드 + 앱 계산 안티패턴과 격리)
 */
@Repository
public interface StatisticsRepository extends JpaRepository<Diary, Long> {

  /** 회원별 일자별 일지 수 집계. 기간이 범위로 제한되므로 결과는 활성 일자 수(최대 기간 길이)만큼만 반환된다. */
  @Query(
      """
      select d.completedDate as bucket, count(d) as count
      from Diary d
      where d.member.id = :memberId
        and d.isDeleted = false
        and d.completedDate between :from and :to
      group by d.completedDate
      """)
  List<DailyCount> aggregateDailyCounts(
      @Param("memberId") Long memberId, @Param("from") LocalDate from, @Param("to") LocalDate to);

  /** 감정별 일지 수 집계. challengeId/기간은 선택(null 이면 전체). feeling 이 null 인 행은 미선택으로 앱에서 병합. */
  @Query(
      """
      select d.feeling as feeling, count(d) as count
      from Diary d
      where d.member.id = :memberId
        and d.isDeleted = false
        and (:from is null or d.completedDate >= :from)
        and (:to is null or d.completedDate <= :to)
        and (:challengeId is null or d.challenge.id = :challengeId)
      group by d.feeling
      """)
  List<FeelingCount> aggregateFeelings(
      @Param("memberId") Long memberId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("challengeId") Long challengeId);

  /** 기간 내 완료 목표 수. */
  @Query(
      """
      select count(g)
      from DiaryGoal g
      where g.diary.member.id = :memberId
        and g.diary.isDeleted = false
        and g.isCompleted = true
        and g.diary.completedDate between :from and :to
      """)
  long countCompletedGoals(
      @Param("memberId") Long memberId, @Param("from") LocalDate from, @Param("to") LocalDate to);

  /** 기간 내 전체 목표 수(완료율 분모). */
  @Query(
      """
      select count(g)
      from DiaryGoal g
      where g.diary.member.id = :memberId
        and g.diary.isDeleted = false
        and g.diary.completedDate between :from and :to
      """)
  long countTotalGoals(
      @Param("memberId") Long memberId, @Param("from") LocalDate from, @Param("to") LocalDate to);

  /** 친구 비교용: 회원 IN 목록에 대한 일지 수 배치 집계(N+1 방지). */
  @Query(
      """
      select d.member.id as memberId, count(d) as count
      from Diary d
      where d.member.id in :memberIds
        and d.isDeleted = false
        and d.completedDate between :from and :to
      group by d.member.id
      """)
  List<MemberCount> countDiariesByMembers(
      @Param("memberIds") Collection<Long> memberIds,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);

  /** 친구 비교용: 회원 IN 목록에 대한 완료 목표 수 배치 집계(N+1 방지). */
  @Query(
      """
      select g.diary.member.id as memberId, count(g) as count
      from DiaryGoal g
      where g.diary.member.id in :memberIds
        and g.diary.isDeleted = false
        and g.isCompleted = true
        and g.diary.completedDate between :from and :to
      group by g.diary.member.id
      """)
  List<MemberCount> countCompletedGoalsByMembers(
      @Param("memberIds") Collection<Long> memberIds,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);

  /** 일자별 일지 수 집계 결과. */
  interface DailyCount {
    LocalDate getBucket();

    long getCount();
  }

  /** 감정별 일지 수 집계 결과. feeling 은 null(미선택) 가능. */
  interface FeelingCount {
    Feeling getFeeling();

    long getCount();
  }

  /** 회원별 수 집계 결과(일지 수/완료 목표 수 공용). */
  interface MemberCount {
    Long getMemberId();

    long getCount();
  }
}
