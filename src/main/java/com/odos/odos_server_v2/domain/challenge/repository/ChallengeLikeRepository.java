package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeLikeRepository extends JpaRepository<ChallengeLike, Long> {
  long countByChallengeId(Long challengeId);

  boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);
}
