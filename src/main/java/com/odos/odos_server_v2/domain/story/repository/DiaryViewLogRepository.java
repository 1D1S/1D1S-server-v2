package com.odos.odos_server_v2.domain.story.repository;

import com.odos.odos_server_v2.domain.story.entity.DiaryViewLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryViewLogRepository extends JpaRepository<DiaryViewLog, Long> {

  // 특정 회원이 특정 일지를 시청했는지 확인
  boolean existsByMemberIdAndDiaryId(Long memberId, Long diaryId);

  List<DiaryViewLog> findByMemberIdAndDiaryIdIn(Long memberId, List<Long> diaryIds);
}
