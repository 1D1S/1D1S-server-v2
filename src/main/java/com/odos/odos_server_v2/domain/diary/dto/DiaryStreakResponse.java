package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryStreakResponse {
  private Long diaryId;
  private String diaryTitle;
  private LocalDate diaryCreatedDate;

  public DiaryStreakResponse checkStreak(Diary diary) {
    return DiaryStreakResponse.builder()
        .diaryId(diary.getId())
        .diaryTitle(diary.getTitle())
        .diaryCreatedDate(diary.getCreatedDate())
        .build();
  }
}
