package com.odos.odos_server_v2.domain.comment.dto;

import com.odos.odos_server_v2.domain.comment.entity.Enum.CommentReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentReportRequest {

  @Schema(description = "신고 유형", example = "BAD_CONTENT")
  private CommentReportType reportType;

  @Schema(description = "신고 내용 (기타 사유 등)", example = "욕설이 포함된 댓글입니다.")
  private String content;
}
