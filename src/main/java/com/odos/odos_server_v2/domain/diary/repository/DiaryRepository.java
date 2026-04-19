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
        order by d.createdAt desc, d.id desc
        """)
  List<Diary> findOthersPublicDiaries(@Param("memberId") Long memberId);

  @Query(
      """
            select d
            from Diary d
            where d.isPublic = true and d.member.id = :memberId
            order by d.createdAt desc, d.id desc
            """)
  Page<Diary> findOthersPublicDiariesByOffset(@Param("memberId") Long memberId, Pageable pageable);

  Page<Diary> findDiariesByChallengeIdAndIsPublic(
      Long challengeId, Boolean isPublic, Pageable pageable);

  // 같은 챌린지의 참여자이면 비공개일지까지 조회되도록
  Page<Diary> findAllByChallengeId(Long challengeId, Pageable pageable);

  // 특정 날짜의 일지 (완료날짜 기준)
  @Query("select d from Diary d where d.completedDate=:completedDate")
  Page<Diary> findDiariesWithCompletedDate(
      @Param("completedDate") LocalDate startDate, Pageable pageable);

  @Query("select d from Diary d where d.createdAt=:createdAt")
  Page<Diary> findDiariesWithCreatedDate(
      @Param("createdAt") LocalDate createdAt, Pageable pageable);

  @Query(
      "select d from Diary d where d.completedDate between :startDate and :endDate and d.member.id=:writer and d.challenge.id=:id")
  List<Diary> findDiariesWithDateRange(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("id") Long id,
      @Param("writer") Long writer);
}
