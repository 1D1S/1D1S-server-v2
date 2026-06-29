package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengePoke;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengePokeRepository extends JpaRepository<ChallengePoke, Long> {

  boolean existsByChallengeIdAndActorIdAndReceiverIdAndPokedDate(
      Long challengeId, Long actorId, Long receiverId, LocalDate pokedDate);

  List<ChallengePoke> findChallengePokesByChallengeIdAndActorIdAndPokedDate(
      Long challengeId, Long actorId, LocalDate pokedDate);
}
