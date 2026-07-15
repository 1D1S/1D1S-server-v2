package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import com.odos.odos_server_v2.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeLikeRepository extends JpaRepository<ChallengeLike, Long> {
  long countByChallengeId(Long challengeId);

  boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

  ChallengeLike findByChallengeIdAndMemberId(Long challengeId, Long memberId);

  // 좋아요 누른 회원 목록(어드민). SELECT cl.member 는 member 를 함께 로딩해 N+1 없이 단일 쿼리로 조회한다.
  // 회원 중복 없음은 V36 유니크 제약((challenge_id, member_id))이 보장하므로 distinct 불필요.
  // (distinct + order by 는 Postgres 에서 order 컬럼이 select 목록에 없다며 거부한다.)
  @Query(
      value =
          "SELECT cl.member FROM ChallengeLike cl "
              + "WHERE cl.challenge.id = :challengeId ORDER BY cl.member.id",
      countQuery = "SELECT COUNT(cl) FROM ChallengeLike cl WHERE cl.challenge.id = :challengeId")
  Page<Member> findLikerMembers(@Param("challengeId") Long challengeId, Pageable pageable);
}
