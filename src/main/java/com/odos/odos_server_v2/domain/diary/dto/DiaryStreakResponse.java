package com.odos.odos_server_v2.domain.diary.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.odos.odos_server_v2.domain.diary.entity.Diary;

@Getter
@Builder
public class DiaryStreakResponse {
  private List<LocalDate> challengeCompletedDate;

  public static DiaryStreakResponse checkStreak(List<Diary> diary) {
    List<LocalDate> diaryCreatedList = diary.stream().map(Diary::getCompletedDate).toList();
    return DiaryStreakResponse.builder().challengeCompletedDate(diaryCreatedList).build();
  }
}
