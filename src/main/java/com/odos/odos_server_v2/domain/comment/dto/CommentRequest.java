package com.odos.odos_server_v2.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequest {

  @Schema(description = "댓글 내용", example = "좋은 일지네요!")
  private String content;
}
