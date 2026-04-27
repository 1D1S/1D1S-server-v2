package com.odos.odos_server_v2.domain.friend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.odos.odos_server_v2.domain.friend.entity.Enum.FriendRequestStatus;
import com.odos.odos_server_v2.domain.friend.entity.FriendRequest;
import com.odos.odos_server_v2.domain.member.entity.Member;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    // 특정 회원이 받은 친구 신청 목록 (수락/거절되지 않은)
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.toMember = :member AND fr.status = :status")
    List<FriendRequest> findByToMemberAndStatus(@Param("member") Member member, @Param("status") FriendRequestStatus status);

    // 특정 회원이 보낸 친구 신청 목록
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.fromMember = :member")
    List<FriendRequest> findByFromMember(@Param("member") Member member);

    // 두 회원 간의 친구 신청 조회 (from -> to)
    Optional<FriendRequest> findByFromMemberAndToMember(Member fromMember, Member toMember);

    // 특정 회원이 받은 모든 친구 신청 목록
    List<FriendRequest> findByToMember(Member toMember);

    // 특정 회원이 보낸 대기 중인 친구 신청 존재 여부 확인
    boolean existsByFromMemberAndToMemberAndStatus(Member fromMember, Member toMember, FriendRequestStatus status);
}
