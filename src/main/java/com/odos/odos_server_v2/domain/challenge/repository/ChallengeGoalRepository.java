package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeGoalRepository extends JpaRepository<ChallengeGoal, Long> {
  long countByParticipantId(Long participantId);
}
