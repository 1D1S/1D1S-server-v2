package com.odos.odos_server_v2.domain.story.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리 목록 응답")
@Builder
@Getter
public class StoryResponseDto {

  @Schema(description = "스토리 그룹 목록 (친구별 묶음)")
  private List<StoryGroupDto> storyGroups;

  @Schema(description = "미시청 스토리 수", example = "3")
  private int unreadCount;
}
