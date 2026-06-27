package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
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
       and c.challengeType != :excludeType
       and (:challengeType is null or c.challengeType = :challengeType)
     order by c.id desc
  """)
  List<Challenge> searchPage(
      @Param("cursorId") Long cursorId,
      @Param("keyword") String keyword,
      @Param("excludeType") ChallengeType excludeType,
      @Param("challengeType") ChallengeType challengeType,
      Pageable pageable);

  @Query(
      """
        SELECT c FROM Challenge c
        WHERE (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%'))
          AND (:category IS NULL OR c.category = :category)
          AND c.challengeType != :excludeType
          AND (:challengeType IS NULL OR c.challengeType = :challengeType)
        ORDER BY c.id DESC
        """)
  Page<Challenge> findByFilters(
      @Param("keyword") String keyword,
      @Param("category") Category category,
      @Param("excludeType") ChallengeType excludeType,
      @Param("challengeType") ChallengeType challengeType,
      Pageable pageable);

  List<Challenge> findByHostMemberId(Long memberId);

  List<Challenge> findByHostMemberIdAndParticipationTypeAndDeletedAtIsNotNull(
      Long memberId, ParticipationType participationType);

  @Query(
      """
      SELECT c FROM Challenge c
      WHERE c.deletedAt IS NULL
        AND (:category IS NULL OR c.category = :category)
        AND (:authorNickname IS NULL OR c.hostMember.nickname LIKE CONCAT('%', :authorNickname, '%'))
        AND (:status IS NULL
             OR (:status = 'ONGOING' AND c.startDate <= :today AND (c.endDate IS NULL OR c.endDate >= :today))
             OR (:status = 'UPCOMING' AND c.startDate > :today)
             OR (:status = 'ENDED' AND c.endDate IS NOT NULL AND c.endDate < :today))
      ORDER BY c.createdAt DESC
      """)
  Page<Challenge> findAdminChallengesOrderByLatest(
      @Param("status") String status,
      @Param("category") Category category,
      @Param("authorNickname") String authorNickname,
      @Param("today") LocalDate today,
      Pageable pageable);

  @Query(
      value =
          """
          SELECT c FROM Challenge c
          LEFT JOIN c.likes l
          WHERE c.deletedAt IS NULL
            AND (:category IS NULL OR c.category = :category)
            AND (:authorNickname IS NULL OR c.hostMember.nickname LIKE CONCAT('%', :authorNickname, '%'))
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
            AND (:category IS NULL OR c.category = :category)
            AND (:authorNickname IS NULL OR c.hostMember.nickname LIKE CONCAT('%', :authorNickname, '%'))
            AND (:status IS NULL
                 OR (:status = 'ONGOING' AND c.startDate <= :today AND (c.endDate IS NULL OR c.endDate >= :today))
                 OR (:status = 'UPCOMING' AND c.startDate > :today)
                 OR (:status = 'ENDED' AND c.endDate IS NOT NULL AND c.endDate < :today))
          """)
  Page<Challenge> findAdminChallengesOrderByLikes(
      @Param("status") String status,
      @Param("category") Category category,
      @Param("authorNickname") String authorNickname,
      @Param("today") LocalDate today,
      Pageable pageable);
}
