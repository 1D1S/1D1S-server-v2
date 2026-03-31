package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
  List<Diary> findDiariesByIsPublic(Boolean isPublic);

  Page<Diary> findDiariesByMember_Id(Long memberId, Pageable pageable);

  long countByChallengeIdAndIsAllGoalsCompletedTrue(Long challengeId);

  // DiaryRepository.java
  @Query(
      """
      select d
      from Diary d
      where d.isPublic = true
        and (:cursorId is null or d.id < :cursorId)
      order by d.id desc
    """)
  List<Diary> findPublicPage(@Param("cursorId") Long cursorId, Pageable pageable);

  @Query(
      "select d from Diary d where d.completedDate between :startDate and :endDate and d.member.id=:writer and d.challenge.id=:id")
  List<Diary> findDiariesByDateRange(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("id") Long id,
      @Param("writer") Long writer);

  @Query(
      """
        select d
        from Diary d
        where d.isPublic = true and d.member.id = :memberId
        """)
  List<Diary> findOthersPublicDiaries(@Param("memberId") Long memberId);

  @Query(
      """
            select d
            from Diary d
            where d.isPublic = true and d.member.id = :memberId
            """)
  Page<Diary> findOthersPublicDiariesByOffset(@Param("memberId") Long memberId, Pageable pageable);

  Page<Diary> findDiariesByChallengeIdAndIsPublic(
      Long challengeId, Boolean isPublic, Pageable pageable);
}
