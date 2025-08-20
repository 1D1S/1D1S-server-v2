package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long> {
  List<DiaryLike> getDiaryLikeCountByDiaryId(Long diaryId);

  Optional<DiaryLike> findDiaryLikeByDiaryIdAndMemberId(Long diaryId, Long memberId);
}
