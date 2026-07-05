package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeGoalRepository extends JpaRepository<ChallengeGoal, Long> {
  long countByParticipantId(Long participantId);

  void deleteAllByParticipantIn(List<Participant> participants);

  void deleteAllByParticipant(Participant participant);

  List<ChallengeGoal> findAllByParticipant_Member_Id(Long memberId);

  List<ChallengeGoal> findByParticipantId(Long participantId);
}
