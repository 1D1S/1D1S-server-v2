package com.odos.odos_server_v2.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "스토리 목록 응답")
@Builder
@Getter
public class StoryResponseDto {

  @Schema(description = "작성자별 스토리 그룹 목록. 본인 그룹이 존재하면 첫 번째로 반환됩니다.")
  private List<StoryGroupDto> storyGroups;

  @Schema(description = "친구의 미시청 스토리 수. 본인 스토리는 집계에서 제외됩니다.", example = "2")
  private int unreadCount;
}
