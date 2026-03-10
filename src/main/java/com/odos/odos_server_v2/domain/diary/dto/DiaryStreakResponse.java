package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryStreakResponse {
  private List<LocalDate> diaryCreatedDate;

  public static DiaryStreakResponse checkStreak(List<Diary> diary) {
    List<LocalDate> diaryCreatedList = diary.stream().map(Diary::getCreatedDate).toList();
    return DiaryStreakResponse.builder().diaryCreatedDate(diaryCreatedList).build();
  }
}
