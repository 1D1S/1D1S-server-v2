package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeGoalRepository extends JpaRepository<ChallengeGoal, Long> {
  long countByParticipantId(Long participantId);

  // 회원의 '오늘 진행 중' 챌린지 목표 수. 기존엔 findAll() 로 전체 challenge_goal 을 적재 후
  // 스트림 필터(+ participant/member/challenge 지연로딩 N+1)했다. DB COUNT 한 번으로 대체.
  // 진행 중 = start_date <= today AND (end_date is null or end_date >= today).
  @Query(
      """
      select count(g) from ChallengeGoal g
        join g.participant p
        join p.challenge c
       where p.member.id = :memberId
         and c.startDate <= :today
         and (c.endDate is null or c.endDate >= :today)
      """)
  long countTodayInProgressGoals(@Param("memberId") Long memberId, @Param("today") LocalDate today);

  void deleteAllByParticipantIn(List<Participant> participants);

  void deleteAllByParticipant(Participant participant);

  List<ChallengeGoal> findAllByParticipant_Member_Id(Long memberId);

  List<ChallengeGoal> findByParticipantId(Long participantId);
}
