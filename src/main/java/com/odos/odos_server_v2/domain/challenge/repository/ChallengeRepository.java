package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

  // "오늘 시작해볼 챌린지": 비공개/삭제/종료 제외한 진행중·예정 챌린지 중 무작위 N건(Pageable 로 상한).
  // 기존엔 findAll() 로 전체 challenge 를 JVM 에 적재 후 필터·셔플했다(대용량 시 OOM/지연).
  // 종료 판정 = endDate < today. 무기한(endDate is null 또는 미래 센티널)은 endDate >= today 로 포함.
  @Query(
      """
      select c from Challenge c
       where cast(c.challengeType as string) <> 'PRIVATE'
         and c.deletedAt is null
         and (c.visibleFrom is null or c.visibleFrom <= :now)
         and (c.endDate is null or c.endDate >= :today)
       order by function('random')
      """)
  List<Challenge> findRandomActiveChallenges(
      @Param("today") LocalDate today, @Param("now") LocalDateTime now, Pageable pageable);

  @Query(
      """
    select c
      from Challenge c
     where c.deletedAt is null
       and (:includeHidden = true or c.visibleFrom is null or c.visibleFrom <= :now)
       and (:cursorId is null or c.id < :cursorId)
       and ( :keyword = ''
             or lower(c.title) like concat('%', lower(:keyword), '%')
             or lower(c.description) like concat('%', lower(:keyword), '%') )
       and cast(c.challengeType as string) != :excludeTypeName
       and (:challengeTypeName is null or cast(c.challengeType as string) = :challengeTypeName)
       and (:allCategory = true or cast(c.category as string) in :categoryNames)
       and ( :allStatus = true
             or ('ONGOING' in :statuses and c.startDate <= :today and (c.endDate is null or c.endDate >= :today))
             or ('UPCOMING' in :statuses and c.startDate > :today)
             or ('ENDED' in :statuses and c.endDate is not null and c.endDate < :today) )
     order by c.id desc
  """)
  List<Challenge> searchPage(
      @Param("cursorId") Long cursorId,
      @Param("keyword") String keyword,
      @Param("excludeTypeName") String excludeTypeName,
      @Param("challengeTypeName") String challengeTypeName,
      @Param("allCategory") boolean allCategory,
      @Param("categoryNames") List<String> categoryNames,
      @Param("allStatus") boolean allStatus,
      @Param("statuses") List<String> statuses,
      @Param("today") LocalDate today,
      @Param("now") LocalDateTime now,
      @Param("includeHidden") boolean includeHidden,
      Pageable pageable);

  @Query(
      """
            SELECT c FROM Challenge c
            WHERE c.deletedAt IS NULL
              AND (:includeHidden = true OR c.visibleFrom IS NULL OR c.visibleFrom <= :now)
              AND (:keyword IS NULL OR c.title LIKE CONCAT('%', CAST(:keyword AS string), '%'))
              AND (:allCategory = true OR CAST(c.category AS string) IN :categoryNames)
              AND CAST(c.challengeType AS string) != :excludeTypeName
              AND (:challengeTypeName IS NULL OR CAST(c.challengeType AS string) = :challengeTypeName)
              AND ( :allStatus = true
                    OR ('ONGOING' IN :statuses AND c.startDate <= :today AND (c.endDate IS NULL OR c.endDate >= :today))
                    OR ('UPCOMING' IN :statuses AND c.startDate > :today)
                    OR ('ENDED' IN :statuses AND c.endDate IS NOT NULL AND c.endDate < :today) )
            ORDER BY c.id DESC
            """)
  Page<Challenge> findByFilters(
      @Param("keyword") String keyword,
      @Param("allCategory") boolean allCategory,
      @Param("categoryNames") List<String> categoryNames,
      @Param("excludeTypeName") String excludeTypeName,
      @Param("challengeTypeName") String challengeTypeName,
      @Param("allStatus") boolean allStatus,
      @Param("statuses") List<String> statuses,
      @Param("today") LocalDate today,
      @Param("now") LocalDateTime now,
      @Param("includeHidden") boolean includeHidden,
      Pageable pageable);

  List<Challenge> findByHostMemberId(Long memberId);

  List<Challenge> findByHostMemberIdAndParticipationTypeAndDeletedAtIsNotNull(
      Long memberId, ParticipationType participationType);

  @Query(
      """
      SELECT c FROM Challenge c
      WHERE c.deletedAt IS NULL
        AND (:categoryName IS NULL OR CAST(c.category AS string) = :categoryName)
        AND (:hostNickname IS NULL OR c.hostMember.nickname LIKE CONCAT('%', CAST(:hostNickname AS string), '%'))
        AND (:status IS NULL
             OR (:status = 'ONGOING' AND c.startDate <= :today AND (c.endDate IS NULL OR c.endDate >= :today))
             OR (:status = 'UPCOMING' AND c.startDate > :today)
             OR (:status = 'ENDED' AND c.endDate IS NOT NULL AND c.endDate < :today))
      ORDER BY c.createdAt DESC
      """)
  Page<Challenge> findAdminChallengesOrderByLatest(
      @Param("status") String status,
      @Param("categoryName") String categoryName,
      @Param("hostNickname") String hostNickname,
      @Param("today") LocalDate today,
      Pageable pageable);

  @Query(
      value =
          """
          SELECT c FROM Challenge c
          LEFT JOIN c.likes l
          WHERE c.deletedAt IS NULL
            AND (:categoryName IS NULL OR CAST(c.category AS string) = :categoryName)
            AND (:hostNickname IS NULL OR c.hostMember.nickname LIKE CONCAT('%', CAST(:hostNickname AS string), '%'))
            AND (:status IS NULL
                 OR (:status = 'ONGOING' AND c.startDate <= :today AND (c.endDate IS NULL OR c.endDate >= :today))
                 OR (:status = 'UPCOMING' AND c.startDate > :today)
                 OR (:status = 'ENDED' AND c.endDate IS NOT NULL AND c.endDate < :today))
          GROUP BY c
          ORDER BY COUNT(l) DESC, c.createdAt DESC
          """,
      countQuery =
          """
          SELECT COUNT(DISTINCT c) FROM Challenge c
          WHERE c.deletedAt IS NULL
            AND (:categoryName IS NULL OR CAST(c.category AS string) = :categoryName)
            AND (:hostNickname IS NULL OR c.hostMember.nickname LIKE CONCAT('%', CAST(:hostNickname AS string), '%'))
            AND (:status IS NULL
                 OR (:status = 'ONGOING' AND c.startDate <= :today AND (c.endDate IS NULL OR c.endDate >= :today))
                 OR (:status = 'UPCOMING' AND c.startDate > :today)
                 OR (:status = 'ENDED' AND c.endDate IS NOT NULL AND c.endDate < :today))
          """)
  Page<Challenge> findAdminChallengesOrderByLikes(
      @Param("status") String status,
      @Param("categoryName") String categoryName,
      @Param("hostNickname") String hostNickname,
      @Param("today") LocalDate today,
      Pageable pageable);

  /**
   * 진행중(삭제/종료되지 않은)이면서 마지막 활동이 비활성 기준일 이전인 챌린지 id 목록. 활동 = 삭제되지 않은 일지의 작성일(createdAt) 중 최신. 일지가 하나도
   * 없으면 챌린지 시작일(startDate)을 기준으로 판정. 공식(OFFICIAL) 챌린지는 대상에서 제외한다.
   */
  @Query(
      """
      select c.id
        from Challenge c
        left join c.diaries d on d.isDeleted = false
       where c.deletedAt is null
         and c.challengeType <> :officialType
         and c.startDate <= :today
         and (c.endDate is null or c.endDate >= :today)
       group by c.id, c.startDate
      having (max(d.createdAt) is not null and max(d.createdAt) < :cutoff)
          or (max(d.createdAt) is null and c.startDate < :cutoffDate)
      """)
  List<Long> findInactiveOngoingChallengeIds(
      @Param("today") LocalDate today,
      @Param("cutoff") LocalDateTime cutoff,
      @Param("cutoffDate") LocalDate cutoffDate,
      @Param("officialType") ChallengeType officialType);

  /** 대상 챌린지를 종료 처리: 종료일(endDate)을 세팅해 기존 ENDED 판정(endDate < today)에 편입시킨다. */
  @Modifying(clearAutomatically = true)
  @Query("update Challenge c set c.endDate = :endDate where c.id in :ids and c.deletedAt is null")
  int closeChallengesByIds(@Param("ids") List<Long> ids, @Param("endDate") LocalDate endDate);
}
