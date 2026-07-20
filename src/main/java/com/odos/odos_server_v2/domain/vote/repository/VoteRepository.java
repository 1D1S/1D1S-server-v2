package com.odos.odos_server_v2.domain.vote.repository;

import com.odos.odos_server_v2.domain.vote.entity.Vote;
import com.odos.odos_server_v2.domain.vote.entity.VoteType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {
  @EntityGraph(attributePaths = "options")
  List<Vote> findAllByVoteTypeOrderByStartDateDescIdDesc(VoteType voteType);

  @EntityGraph(attributePaths = "options")
  List<Vote> findAllByOrderByStartDateDescIdDesc();

  @EntityGraph(attributePaths = "options")
  @Query(
      "select distinct v from Vote v where v.voteType = :type and v.startDate <= :today and v.endDate >= :today order by v.startDate asc, v.id asc")
  List<Vote> findOpenVotes(@Param("type") VoteType type, @Param("today") LocalDate today);

  @EntityGraph(attributePaths = "options")
  @Query(
      "select distinct v from Vote v where v.startDate <= :today and v.endDate >= :today order by v.startDate asc, v.id asc")
  List<Vote> findAllOpenVotes(@Param("today") LocalDate today);

  @Override
  @EntityGraph(attributePaths = "options")
  Optional<Vote> findById(Long id);
}
