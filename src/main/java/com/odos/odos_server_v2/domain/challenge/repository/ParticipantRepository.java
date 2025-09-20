package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
  long countByChallengeIdAndStatusIn(Long challengeId, List<ParticipantStatus> statuses);

  List<Participant> findByChallengeIdAndStatusIn(
      Long challengeId, List<ParticipantStatus> statuses);

  Participant findByMemberIdAndChallengeId(Long memberId, Long challengeId);

  boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);
}
