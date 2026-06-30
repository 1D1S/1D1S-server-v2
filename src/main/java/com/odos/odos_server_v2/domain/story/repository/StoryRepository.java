package com.odos.odos_server_v2.domain.story.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.story.dto.StoryDiarySummaryProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Diary, Long> {

  // 특정 회원 본인과 친구가 24시간 이내 작성한 일지 요약 조회
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
        WHERE (
          d.member.id = :memberId
          OR EXISTS (
            SELECT 1
            FROM Friend f
            WHERE f.member.id = :memberId
            AND f.friendMember = d.member
          )
        )
        AND d.createdAt >= :since
        AND d.isDeleted = false
        ORDER BY d.createdAt
    """)
  List<StoryDiarySummaryProjection> findStoryDiarySummariesWithin24Hours(
      @Param("memberId") Long memberId, @Param("since") LocalDateTime since);
}
