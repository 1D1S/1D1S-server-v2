package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.notification.entity.DiaryLikeMilestone;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryLikeMilestoneRepository extends JpaRepository<DiaryLikeMilestone, Long> {

  Optional<DiaryLikeMilestone> findByDiary(Diary diary);
}
