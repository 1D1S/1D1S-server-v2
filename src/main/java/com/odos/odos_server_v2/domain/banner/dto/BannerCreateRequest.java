package com.odos.odos_server_v2.domain.banner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "배너 생성 요청(관리자)")
@Getter
@Setter
@NoArgsConstructor
public class BannerCreateRequest {

  @Schema(description = "배너 제목", example = "여름 이벤트")
  private String title;

  @Schema(description = "배너 부제목", example = "7월 한정 혜택")
  private String subtitle;

  @Schema(
      description = "배너 이미지 URL(presigned 업로드 후 fileUrl)",
      example = "https://cdn.example.com/banner.png")
  private String imageUrl;

  @Schema(description = "배너 클릭 이동 링크", example = "https://1day1streak.com/event/1")
  private String linkUrl;

  @Schema(description = "게시 시작일", example = "2026-07-01")
  private LocalDate startDate;

  @Schema(description = "게시 종료일", example = "2026-07-09")
  private LocalDate endDate;

  @Schema(description = "배너 태그(선택, 칩 표시용)", example = "이벤트", nullable = true)
  private String tag;
}
