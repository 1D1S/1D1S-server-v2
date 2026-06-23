package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryDeleteAdminResponse {
  private Long diaryId;
  private String diaryTitle;
  private String diaryDeletedReason;

  public static DiaryDeleteAdminResponse from(Diary diary) {
    return DiaryDeleteAdminResponse.builder()
        .diaryId(diary.getId())
        .diaryTitle(diary.getTitle())
        .diaryDeletedReason(diary.getDeletedReason())
        .build();
  }
}
