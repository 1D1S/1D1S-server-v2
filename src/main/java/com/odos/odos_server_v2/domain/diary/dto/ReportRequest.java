package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Enum.ReportType;
import lombok.Getter;

@Getter
public class ReportRequest {
  private Long diaryId;
  private String content;
  private ReportType reportType;
}
