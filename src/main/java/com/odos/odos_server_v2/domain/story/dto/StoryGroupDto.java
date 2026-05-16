package com.odos.odos_server_v2.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "스토리 그룹 응답 (친구별 묶음)")
@Builder
@Getter
public class StoryGroupDto {

  @Schema(description = "친구 회원 ID", example = "2")
  private Long userId;

  @Schema(description = "친구 회원 이름", example = "김나영")
  private String userName;

  @Schema(description = "친구 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImage;

  @Schema(description = "친구의 스토리 목록")
  private List<StoryItemDto> stories;
}
