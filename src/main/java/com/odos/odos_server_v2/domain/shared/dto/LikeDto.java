package com.odos.odos_server_v2.domain.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "좋아요 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDto {

  @Schema(description = "현재 로그인한 사용자의 좋아요 여부", example = "false")
  private boolean likedByMe;

  @Schema(description = "총 좋아요 수", example = "12")
  private long likeCnt;
}
