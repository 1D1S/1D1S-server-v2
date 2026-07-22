package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeDailyCountProjection;
import com.odos.odos_server_v2.domain.challenge.dto.MemberDiaryDateProjection;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
  Optional<Diary> findByIdAndIsDeletedFalse(Long id);

  @EntityGraph(attributePaths = {"member", "challenge"})
  List<Diary> findDiariesByIsPublicAndIsDeletedFalse(Boolean isPublic);

  // 랜덤 공개 일지 N건(Pageable 로 상한). 전체 공개 일지를 JVM 으로 로드해 셔플하면(대용량 시 OOM/지연)
  // 위험하므로 DB 에서 무작위 정렬 후 상한만 가져온다. member/challenge 는 EntityGraph 로 함께 로딩.
  // ponytail: function('random') 은 대상 전체를 정렬하지만 앱으로 넘어오는 행은 상한으로 제한되어
  // JVM 메모리 폭주는 사라진다. 표본이 매우 커지면 tablesample/키 랜덤 조인으로 교체.
  @EntityGraph(attributePaths = {"member", "challenge"})
  @Query(
      "select d from Diary d where d.isPublic = true and d.isDeleted = false order by function('random')")
  List<Diary> findRandomPublicDiaries(Pageable pageable);

  List<Diary> findDiariesByMember_Id(Long memberId);

  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findDiariesByMember_IdAndIsDeletedFalse(Long memberId, Pageable pageable);

  @EntityGraph(attributePaths = {"member", "challenge"})
  List<Diary> findDiariesByMember_IdAndIsDeletedFalse(Long memberId);

  /**
   * 위젯/스트릭용: 회원이 일지를 쓴 날짜 목록(중복 제거, 삭제 제외). 엔티티를 통째로 로드하는 {@link
   * #findDiariesByMember_IdAndIsDeletedFalse(Long)} 와 달리 날짜 컬럼만 투영하므로, 주기 호출되는 위젯에서 가볍다.
   * idx_diary_member_completed_date(member_id, completed_date) 로 커버된다.
   */
  @Query(
      """
      select distinct d.completedDate
      from Diary d
      where d.member.id = :memberId
        and d.isDeleted = false
      """)
  List<LocalDate> findCompletedDatesByMemberId(@Param("memberId") Long memberId);

  long countByChallengeIdAndIsAllGoalsCompletedTrueAndIsDeletedFalse(Long challengeId);

  /** 챌린지 랭킹용: 챌린지 내 멤버별 일지 작성 날짜를 한 번에 조회(스트릭 계산용, 참여자당 반복 조회 방지). */
  @Query(
      """
      select d.member.id as memberId, d.completedDate as completedDate
      from Diary d
      where d.challenge.id = :challengeId and d.isDeleted = false
      """)
  List<MemberDiaryDateProjection> findMemberDiaryDatesForChallenge(
      @Param("challengeId") Long challengeId);

  /** 챌린지 통계용: 기간 내 날짜별 일지 개수(일지 추이). completedDate 기준, 삭제 제외. */
  @Query(
      """
      select d.completedDate as bucket, count(d) as cnt
      from Diary d
      where d.challenge.id = :challengeId
        and d.isDeleted = false
        and d.completedDate between :from and :to
      group by d.completedDate
      """)
  List<ChallengeDailyCountProjection> countDiariesByDateForChallenge(
      @Param("challengeId") Long challengeId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);

  /** 챌린지 일지 리스트(참여자/호스트용): 특정 날짜(completedDate)로 필터. */
  Page<Diary> findAllByChallengeIdAndCompletedDateAndIsDeletedFalse(
      Long challengeId, LocalDate completedDate, Pageable pageable);

  /** 챌린지 일지 리스트(비참여자용): 특정 날짜(completedDate) + 공개 일지만. */
  Page<Diary> findByChallengeIdAndIsPublicAndCompletedDateAndIsDeletedFalse(
      Long challengeId, Boolean isPublic, LocalDate completedDate, Pageable pageable);

  @Query(
      """
      select c.diary.id, count(c)
      from Comment c
      where c.diary.id in :diaryIds and c.isDeleted=false
      group by c.diary.id
      """)
  List<Object[]> countCommentsByDiaryIds(@Param("diaryIds") Collection<Long> diaryIds);

  boolean existsByChallengeIdAndMemberIdAndCompletedDateAndIsDeletedFalse(
      Long challengeId, Long memberId, LocalDate completedDate);

  /**
   * 홈 '오늘의 기록'용: 주어진 챌린지들 중 회원이 오늘(completedDate) 일지를 작성한 챌린지 id 목록. 챌린지별 exists 반복(N+1) 대신 IN 절 한
   * 번으로 처리한다. idx_diary_member_completed_date(member_id, completed_date) 로 커버.
   */
  @Query(
      """
      select distinct d.challenge.id
      from Diary d
      where d.member.id = :memberId
        and d.challenge.id in :challengeIds
        and d.completedDate = :date
        and d.isDeleted = false
      """)
  List<Long> findChallengeIdsWithDiaryOnDate(
      @Param("memberId") Long memberId,
      @Param("challengeIds") Collection<Long> challengeIds,
      @Param("date") LocalDate date);

  @Query(
      """
      select d
      from Diary d
      where d.isPublic = true and d.isDeleted = false
        and (:cursorId is null or d.id < :cursorId)
      order by d.id desc
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  List<Diary> findPublicPage(@Param("cursorId") Long cursorId, Pageable pageable);

  @Query(
      """
      select d
      from Diary d
      where d.completedDate between :startDate and :endDate
        and d.member.id = :writer
        and d.challenge.id = :id
        and d.isDeleted = false
      """)
  List<Diary> findDiariesByDateRange(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("id") Long id,
      @Param("writer") Long writer);

  @Query(
      """
      select d
      from Diary d
      where d.isPublic = true and d.member.id = :memberId and d.isDeleted = false
      order by d.createdAt desc, d.id desc
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  List<Diary> findOthersPublicDiaries(@Param("memberId") Long memberId);

  @Query(
      """
      select d
      from Diary d
      where d.isPublic = true and d.member.id = :memberId and d.isDeleted = false
      order by d.createdAt desc, d.id desc
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findOthersPublicDiariesByOffset(@Param("memberId") Long memberId, Pageable pageable);

  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findDiariesByChallengeIdAndIsPublicAndIsDeletedFalse(
      Long challengeId, Boolean isPublic, Pageable pageable);

  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findAllByChallengeIdAndIsDeletedFalse(Long challengeId, Pageable pageable);

  @Query(
      """
      select d
      from Diary d
      order by d.createdAt desc, d.id desc
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findAdminDiariesOrderByLatest(Pageable pageable);

  @Query(
      """
      select d
      from Diary d
      where d.member.nickname like concat('%', :authorNickname, '%')
      order by d.createdAt desc, d.id desc
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findAdminDiariesByAuthorNicknameOrderByLatest(
      @Param("authorNickname") String authorNickname, Pageable pageable);

  @Query(
      """
      select d
      from Diary d
      where d.challenge.id = :challengeId
      order by d.createdAt desc, d.id desc
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findAdminDiariesByChallengeIdOrderByLatest(
      @Param("challengeId") Long challengeId, Pageable pageable);

  @Query(
      value =
          """
          select d
          from Diary d
          left join d.likes l
          group by d
          order by count(l) desc, d.createdAt desc, d.id desc
          """,
      countQuery =
          """
          select count(d)
          from Diary d
          """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findAdminDiariesOrderByLike(Pageable pageable);

  @Query("select d from Diary d where d.completedDate = :completedDate and d.isDeleted = false")
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findDiariesWithCompletedDate(
      @Param("completedDate") LocalDate startDate, Pageable pageable);

  @Query(
      """
      select d
      from Diary d
      where (:createdAt is null or cast(d.createdAt as LocalDate) = :createdAt)
        and d.isDeleted = false
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findDiariesWithCreatedDate(
      @Param("createdAt") LocalDate createdAt, Pageable pageable);

  @Query(
      """
      select d
      from Diary d
      where d.completedDate between :startDate and :endDate
        and d.isDeleted = false
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
  Page<Diary> findDiariesByDateRangeWithCompletedDate(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      Pageable pageable);

  @Query(
      """
      select d
      from Diary d
      where (:startDate is null or cast(d.createdAt as LocalDate) >= :startDate)
        and (:endDate is null or cast(d.createdAt as LocalDate) <= :endDate)
        and d.isDeleted = false
      """)
  @EntityGraph(attributePaths = {"member", "challenge"})
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

  @Modifying
  @Query(
      """
            update Diary d
            set d.isDeleted = true
            where d.challenge.id = :challengeId
              and d.isDeleted = false
            """)
  void softDeleteByChallengeId(@Param("challengeId") Long challengeId);
}
