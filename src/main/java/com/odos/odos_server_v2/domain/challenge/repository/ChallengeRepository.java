package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
  @Query(
      """
    select c
      from Challenge c
     where (:cursorId is null or c.id < :cursorId)
       and ( :keyword = ''
             or lower(c.title) like concat('%', lower(:keyword), '%')
             or lower(c.description) like concat('%', lower(:keyword), '%') )
       and cast(c.challengeType as string) != :excludeTypeName
       and (:challengeTypeName is null or cast(c.challengeType as string) = :challengeTypeName)
       and (:categoryName is null or cast(c.category as string) = :categoryName)
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
      @Param("categoryName") String categoryName,
      @Param("allStatus") boolean allStatus,
      @Param("statuses") List<String> statuses,
      @Param("today") LocalDate today,
      Pageable pageable);

  @Query(
      """
            SELECT c FROM Challenge c
            WHERE (:keyword IS NULL OR c.title LIKE CONCAT('%', CAST(:keyword AS string), '%'))
              AND (:categoryName IS NULL OR CAST(c.category AS string) = :categoryName)
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
      @Param("categoryName") String categoryName,
      @Param("excludeTypeName") String excludeTypeName,
      @Param("challengeTypeName") String challengeTypeName,
      @Param("allStatus") boolean allStatus,
      @Param("statuses") List<String> statuses,
      @Param("today") LocalDate today,
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
}
