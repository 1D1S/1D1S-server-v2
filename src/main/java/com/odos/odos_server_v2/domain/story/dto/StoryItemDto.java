package com.odos.odos_server_v2.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "스토리 항목 응답")
@Builder
@Getter
public class StoryItemDto {

  @Schema(description = "일지 ID", example = "10")
  private Long diaryId;

  @Schema(description = "일지 제목")
  private String diaryTitle;

  @Schema(description = "일지 썸네일")
  private String diaryThumbnail;

  @Schema(description = "업로드 시간", example = "2024-01-01T12:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "미시청 여부", example = "true")
  private Boolean hasUnreadJournal;
}
