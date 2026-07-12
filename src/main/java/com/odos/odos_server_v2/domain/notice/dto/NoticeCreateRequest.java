package com.odos.odos_server_v2.domain.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "공지 생성 요청(어드민)")
@Getter
@Setter
@NoArgsConstructor
public class NoticeCreateRequest {

  @Schema(description = "제목", example = "서비스 점검 안내")
  private String title;

  @Schema(description = "내용", example = "7월 15일 02:00~04:00 서버 점검이 예정되어 있습니다.")
  private String content;

  @Schema(description = "고정 여부(미지정 시 false). 고정 공지는 목록 최상단에 노출", example = "false")
  private Boolean pinned;
}
