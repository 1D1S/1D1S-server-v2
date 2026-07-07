package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.FixedChallengeGoal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedChallengeGoalRepository extends JpaRepository<FixedChallengeGoal, Long> {

  List<FixedChallengeGoal> findByChallengeId(Long challengeId);

  long countByChallengeId(Long challengeId);

  void deleteAllByChallengeId(Long challengeId);
}
