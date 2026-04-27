package com.odos.odos_server_v2.domain.friend.repository;

import com.odos.odos_server_v2.domain.friend.entity.BlockList;
import com.odos.odos_server_v2.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockListRepository extends JpaRepository<BlockList, Long> {

  // 특정 회원의 차단 목록 조회
  List<BlockList> findByMember(Member member);

  // 특정 회원과 차단된 회원 관계 조회
  Optional<BlockList> findByMemberAndBlockedMember(Member member, Member blockedMember);

  // 특정 회원이 특정 회원을 차단했는지 확인
  boolean existsByMemberAndBlockedMember(Member member, Member blockedMember);

  // 차단 수 count
  long countByMember(Member member);
}
