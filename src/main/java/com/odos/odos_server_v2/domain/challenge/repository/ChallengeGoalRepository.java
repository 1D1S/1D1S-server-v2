package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeGoalRepository extends JpaRepository<ChallengeGoal, Long> {
  long countByParticipantId(Long participantId);

  @Query(
      "select cg from ChallengeGoal cg "
          + "join cg.participant p "
          + "join p.challenge c where c.hostMember.id=:memberId and c.id=:challengeId and p.member.id=:memberId")
  List<ChallengeGoal> getFixedGoals(
      @Param("memberId") Long memberId, @Param("challengeId") Long challengeId);

  void deleteAllByParticipantIn(List<Participant> participants);

  void deleteAllByParticipant(Participant participant);

  List<ChallengeGoal> findAllByParticipant_Member_Id(Long memberId);
}
