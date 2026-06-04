package com.odos.odos_server_v2.domain.story.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.story.dto.StoryDiarySummaryProjection;

@Repository
public interface StoryRepository extends JpaRepository<Diary, Long> {

  // 특정 회원의 친구 목록 중 24시간 이내 작성된 일지 요약 조회
  @Query(
      """
        SELECT
          d.id AS diaryId,
          d.title AS diaryTitle,
          d.createdAt AS createdAt,
          d.member.id AS memberId,
          d.member.nickname AS memberNickname,
          d.member.profileUrl AS memberProfileUrl
        FROM Diary d
        INNER JOIN Friend f ON f.friendMember = d.member
        WHERE f.member.id = :memberId
        AND d.createdAt >= :since
        AND d.isDeleted = false
        ORDER BY d.createdAt DESC
    """)
  List<StoryDiarySummaryProjection> findFriendDiarySummariesWithin24Hours(
      @Param("memberId") Long memberId, @Param("since") LocalDateTime since);
}
