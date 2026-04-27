package com.odos.odos_server_v2.domain.friend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.odos.odos_server_v2.domain.friend.entity.Friend;
import com.odos.odos_server_v2.domain.member.entity.Member;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 특정 회원의 친구 목록 조회
    @Query("SELECT f FROM Friend f WHERE f.member = :member")
    List<Friend> findByMember(@Param("member") Member member);

    // 특정 회원과 특정 친구의 관계 조회
    Optional<Friend> findByMemberAndFriendMember(Member member, Member friendMember);

    // 특정 회원이 특정 회원을 친구로 가지고 있는지 확인
    boolean existsByMemberAndFriendMember(Member member, Member friendMember);

    // 친구 수 count
    long countByMember(Member member);
}
