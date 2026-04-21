package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
  Optional<Diary> findByIdAndIsDeletedFalse(Long id); // 기존 공개다이어리만 조회에서 softDelete 적용 위해 이거 추가

  List<Diary> findDiariesByIsPublicAndIsDeletedFalse(Boolean isPublic);

  Page<Diary> findDiariesByMember_IdAndIsDeletedFalse(Long memberId, Pageable pageable);

  long countByChallengeIdAndIsAllGoalsCompletedTrue(Long challengeId);

  // DiaryRepository.java
  @Query(
      """
      select d
      from Diary d
      where d.isPublic = true and d.isDeleted=false
        and (:cursorId is null or d.id < :cursorId)
      order by d.id desc
    """)
  List<Diary> findPublicPage(@Param("cursorId") Long cursorId, Pageable pageable);

  @Query(
      "select d from Diary d where d.completedDate between :startDate and :endDate and d.member.id=:writer and d.challenge.id=:id and d.isDeleted=false")
  List<Diary> findDiariesByDateRange(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("id") Long id,
      @Param("writer") Long writer);

  @Query(
      """
        select d
        from Diary d
        where d.isPublic = true and d.member.id = :memberId and d.isDeleted=false
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

  Page<Diary> findDiariesByChallengeIdAndIsPublicAndIsDeletedFalse(
      Long challengeId, Boolean isPublic, Pageable pageable);

  // 같은 챌린지의 참여자이면 비공개일지까지 조회되도록
  Page<Diary> findAllByChallengeIdAndIsDeletedFalse(Long challengeId, Pageable pageable);

  // 특정 날짜의 일지 (완료날짜 기준)
  @Query("select d from Diary d where d.completedDate=:completedDate and d.isDeleted=false")
  Page<Diary> findDiariesWithCompletedDate(
      @Param("completedDate") LocalDate startDate, Pageable pageable);

  @Query(
      """
select d
from Diary d
where (:createdAt is null or cast(d.createdAt as LocalDate) = :createdAt) and d.isDeleted=false
""")
  Page<Diary> findDiariesWithCreatedDate(
      @Param("createdAt") LocalDate createdAt, Pageable pageable);

  @Query(
      "select d from Diary d where d.completedDate between :startDate and :endDate and d.isDeleted=false")
  Page<Diary> findDiariesByDateRangeWithCompletedDate(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      Pageable pageable);

  @Query(
      """
select d
from Diary d
where (:startDate is null or cast(d.createdAt as LocalDate) >= :startDate)
  and (:endDate is null or cast(d.createdAt as LocalDate) <= :endDate) and d.isDeleted=false
""")
  Page<Diary> findDiariesByDateRangeWithCreatedDate(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable page);

  @Modifying
  @Query(
      """
            update Diary d
            set d.isDeleted = true
            where d.challenge.id = :challengeId
              and d.member.id = :memberId
              and d.isDeleted = false
            """)
  void softDeleteByChallengeIdAndMemberId(
      @Param("challengeId") Long challengeId, @Param("memberId") Long memberId);

  @Modifying
  @Query(
      """
            update Diary d
            set d.isDeleted = false
            where d.challenge.id = :challengeId
              and d.member.id = :memberId
              and d.isDeleted = true
            """)
  void restoreByChallengeIdAndMemberId(
      @Param("challengeId") Long challengeId, @Param("memberId") Long memberId);
}
