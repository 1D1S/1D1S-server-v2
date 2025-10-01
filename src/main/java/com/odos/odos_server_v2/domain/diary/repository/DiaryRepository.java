package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
  List<Diary> findDiariesByIsPublic(Boolean isPublic);

  List<Diary> findDiariesByMember_Id(Long memberId);

  long countByChallengeIdAndIsAllGoalsCompletedTrue(Long challengeId);

  // DiaryRepository.java
  @Query(
      """
      select d
      from Diary d
      where d.isPublic = true
        and (:cursorId is null or d.id < :cursorId)
      order by d.id desc
    """)
  List<Diary> findPublicPage(@Param("cursorId") Long cursorId, Pageable pageable);
}
