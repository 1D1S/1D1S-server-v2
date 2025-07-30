package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {}
