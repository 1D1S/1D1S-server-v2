package com.odos.odos_server_v2.domain.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "공지 수정 요청(어드민). 포함한 필드만 변경, 생략 필드는 유지.")
@Getter
@Setter
@NoArgsConstructor
public class NoticeUpdateRequest {

  @Schema(description = "제목")
  private String title;

  @Schema(description = "내용")
  private String content;

  @Schema(description = "고정 여부", example = "true")
  private Boolean pinned;
}
