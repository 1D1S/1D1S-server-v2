package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.challenge.dto.MemberGoalCountProjection;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryGoalRepository extends JpaRepository<DiaryGoal, Long> {
  long countByDiary_Challenge_IdAndIsCompletedTrueAndDiary_IsDeletedFalse(Long challengeId);

  long countByChallengeGoalIdAndDiary_IsDeletedFalse(Long challengeGoalId);

  /** 챌린지 랭킹용: 멤버별 완료 목표 수를 한 번의 group by 로 집계(참여자 수만큼의 N+1 방지). */
  @Query(
      """
      select d.diary.member.id as memberId, count(d) as completedGoalCount
      from DiaryGoal d
      where d.diary.challenge.id = :challengeId
        and d.isCompleted = true
        and d.diary.isDeleted = false
      group by d.diary.member.id
      """)
  List<MemberGoalCountProjection> countCompletedGoalsByMemberForChallenge(
      @Param("challengeId") Long challengeId);
}
