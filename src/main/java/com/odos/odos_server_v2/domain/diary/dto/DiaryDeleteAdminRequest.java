package com.odos.odos_server_v2.domain.diary.dto;

import lombok.Getter;

@Getter
public class DiaryDeleteAdminRequest {
  private Long diaryId;
  private String deleteReason;
}
