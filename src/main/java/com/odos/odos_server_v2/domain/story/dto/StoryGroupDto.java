package com.odos.odos_server_v2.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "스토리 그룹 응답 (작성자별 묶음)")
@Builder
@Getter
public class StoryGroupDto {

  @Schema(description = "작성자 회원 ID", example = "2")
  private Long userId;

  @Schema(description = "작성자 회원 이름", example = "김나영")
  private String userName;

  @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImage;

  @Schema(description = "현재 사용자의 스토리 그룹 여부. 본인 스토리가 없으면 true인 그룹은 반환되지 않습니다.", example = "false")
  private Boolean isMyStory;

  @Schema(description = "작성자의 스토리 목록. 본인은 오래된순, 친구는 미시청 우선이며 같은 시청 상태에서는 오래된순입니다.")
  private List<StoryItemDto> stories;
}
