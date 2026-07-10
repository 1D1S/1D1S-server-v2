package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
  long countByChallengeIdAndStatusIn(Long challengeId, List<ParticipantStatus> statuses);

  List<Participant> findByChallengeIdAndStatusIn(
      Long challengeId, List<ParticipantStatus> statuses);

  List<Participant> findByMemberIdAndStatusIn(Long memberId, List<ParticipantStatus> statuses);

  Optional<Participant> findByMemberIdAndChallengeId(Long memberId, Long challengeId);

  boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

  boolean existsByChallengeIdAndMemberIdAndStatus(
      Long challengeId, Long memberId, ParticipantStatus status);

  List<Participant> findByMemberId(Long memberId);

  @Query(
      """
      select p
      from Participant p
      join fetch p.member
      join fetch p.challenge c
      where p.status in :statuses
        and c.deletedAt is null
        and c.startDate <= :today
        and c.endDate >= :today
        and not exists (
          select 1
          from Diary d
          where d.challenge = c
            and d.member = p.member
            and d.completedDate = :today
            and d.isDeleted = false
        )
      """)
  List<Participant> findTodayDiaryReminderTargets(
      @Param("statuses") List<ParticipantStatus> statuses, @Param("today") LocalDate today);
}
