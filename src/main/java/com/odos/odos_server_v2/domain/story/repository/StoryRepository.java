package com.odos.odos_server_v2.domain.story.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.member.entity.Member;

@Repository
public interface StoryRepository extends JpaRepository<Diary, Long> {

  // 특정 회원의 친구 목록 중 24시간 이내 작성된 일지 조회
  @Query(
      """
        SELECT d FROM Diary d
        INNER JOIN Friend f ON f.friendMember = d.member
        WHERE f.member.id = :memberId
        AND d.createdAt >= :since
        AND d.isDeleted = false
        ORDER BY d.createdAt DESC
    """)
  List<Diary> findFriendDiariesWithin24Hours(
      @Param("memberId") Long memberId, @Param("since") LocalDateTime since);

  // 특정 회원이 특정 친구의 최신 일지 1개 조회
  @Query(
      """
        SELECT d FROM Diary d
        WHERE d.member = :friendMember
        AND d.createdAt >= :since
        AND d.isDeleted = false
        ORDER BY d.createdAt DESC
    """)
  List<Diary> findLatestDiaryByFriend(
      @Param("friendMember") Member friendMember, @Param("since") LocalDateTime since);
}
