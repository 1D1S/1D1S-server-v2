package com.odos.odos_server_v2.domain.diary.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일지 신고 유형")
public enum ReportType {
  @Schema(description = "부적절한 제목/내용")
  BAD_TITLE_CONTENT,
  @Schema(description = "부적절한 사진")
  BAD_IMAGE,
  @Schema(description = "기타")
  ETC
}
