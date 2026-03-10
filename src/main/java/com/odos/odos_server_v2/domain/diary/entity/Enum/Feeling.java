package com.odos.odos_server_v2.domain.diary.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감정 상태")
public enum Feeling {
  @Schema(description = "행복")
  HAPPY,
  @Schema(description = "슬픔")
  SAD,
  @Schema(description = "보통")
  NORMAL,
  @Schema(description = "선택 안함")
  NONE
}
