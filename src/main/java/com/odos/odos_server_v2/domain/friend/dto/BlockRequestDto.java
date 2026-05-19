package com.odos.odos_server_v2.domain.friend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "차단 요청")
@Builder
@Getter
public class BlockRequestDto {

  @Schema(description = "차단할 회원 ID", example = "2")
  private Long blockedMemberId;
}
