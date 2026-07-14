package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import com.odos.odos_server_v2.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long> {
  long countByDiaryId(Long diaryId);

  Optional<DiaryLike> findDiaryLikeByDiaryIdAndMemberId(Long diaryId, Long memberId);

  // 좋아요 누른 회원 목록(어드민). SELECT dl.member 는 member 를 함께 로딩해 N+1 없이 단일 쿼리로 조회한다.
  // 회원 중복 없음은 V36 유니크 제약((diary_id, member_id))이 보장하므로 distinct 불필요.
  // (distinct + order by 는 Postgres 에서 order 컬럼이 select 목록에 없다며 거부한다.)
  @Query(
      value =
          "SELECT dl.member FROM DiaryLike dl WHERE dl.diary.id = :diaryId ORDER BY dl.member.id",
      countQuery = "SELECT COUNT(dl) FROM DiaryLike dl WHERE dl.diary.id = :diaryId")
  Page<Member> findLikerMembers(@Param("diaryId") Long diaryId, Pageable pageable);
}
