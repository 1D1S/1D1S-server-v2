package com.odos.odos_server_v2.domain.vote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.odos.odos_server_v2.domain.vote.entity.VoteResponse;

public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {
  boolean existsByVoteIdAndMemberId(Long voteId, Long memberId);

  long countByVoteId(Long voteId);

  @EntityGraph(attributePaths = {"member", "selectedOptions"})
  List<VoteResponse> findAllByVoteIdOrderByCreatedAtAsc(Long voteId);
}
