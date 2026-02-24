package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Enum.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "일지 신고 요청 DTO")
public class ReportRequest {
  @Schema(description = "일지 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long diaryId;

  @Schema(
      description = "일지 내용",
      example = "오늘은 할일을 무사히 끝냈다",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String content;

  @Schema(
      description = "신고유형",
      example = "BAD_TITLE_CONTENT",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private ReportType reportType;
}
