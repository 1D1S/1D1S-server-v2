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

  // participant 에 (member_id, challenge_id) 유니크 제약이 없어 같은 회원의 중복 참여자 행이
  // 더티 데이터로 존재할 수 있다. Optional 단건 조회는 그 경우 NonUniqueResultException(500) 을 던지므로
  // First+OrderById 로 결정적으로 1건만 반환한다(가장 먼저 생성된 행).
  Optional<Participant> findFirstByMemberIdAndChallengeIdOrderByIdAsc(
      Long memberId, Long challengeId);

  boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

  boolean existsByChallengeIdAndMemberIdAndStatus(
      Long challengeId, Long memberId, ParticipantStatus status);

  List<Participant> findByMemberId(Long memberId);

  // 홈 '오늘의 기록'용: 회원이 진행 중(오늘 기준)인 챌린지의 참여자 + 챌린지 + 목표를 한 번에 로드한다.
  // 진행 중 = start_date <= today AND (end_date is null or end_date >= today), 삭제 챌린지 제외.
  // challengeGoals(OneToMany) 하나만 fetch join 하므로 카테시안 폭증 없이 단일 쿼리로 N+1 을 제거한다.
  @Query(
      """
      select distinct p
      from Participant p
      join fetch p.challenge c
      left join fetch p.challengeGoals
      where p.member.id = :memberId
        and p.status in :statuses
        and c.deletedAt is null
        and c.startDate <= :today
        and (c.endDate is null or c.endDate >= :today)
      order by p.id
      """)
  List<Participant> findInProgressWithGoals(
      @Param("memberId") Long memberId,
      @Param("statuses") List<ParticipantStatus> statuses,
      @Param("today") LocalDate today);

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
