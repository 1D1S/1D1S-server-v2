package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.notification.entity.DiaryLikeMilestoneState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryLikeMilestoneStateRepository
    extends JpaRepository<DiaryLikeMilestoneState, Long> {
  Optional<DiaryLikeMilestoneState> findByDiary(Diary diary);
}
