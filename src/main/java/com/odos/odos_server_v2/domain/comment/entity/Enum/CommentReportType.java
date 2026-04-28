package com.odos.odos_server_v2.domain.comment.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 신고 유형")
public enum CommentReportType {
  @Schema(description = "부적절한 내용")
  BAD_CONTENT,

  @Schema(description = "스팸")
  SPAM,

  @Schema(description = "기타")
  ETC
}
