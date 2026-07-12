package com.odos.odos_server_v2.domain.banner.dto;

import com.odos.odos_server_v2.domain.banner.entity.Banner;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "배너 응답")
@Getter
@Builder
public class BannerResponse {
  @Schema(description = "배너 ID", example = "1")
  private Long id;

  @Schema(description = "배너 제목", example = "여름 이벤트")
  private String title;

  @Schema(description = "배너 부제목", example = "7월 한정 혜택")
  private String subtitle;

  @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/banner.png")
  private String imageUrl;

  @Schema(description = "배너 클릭 이동 링크", example = "https://1day1streak.com/event/1")
  private String linkUrl;

  @Schema(description = "게시 시작일", example = "2026-07-01")
  private LocalDate startDate;

  @Schema(description = "게시 종료일", example = "2026-07-09")
  private LocalDate endDate;

  public static BannerResponse from(Banner banner) {
    return BannerResponse.builder()
        .title(banner.getTitle())
        .subtitle(banner.getSubtitle())
        .imageUrl(banner.getImageUrl())
        .linkUrl(banner.getLinkUrl())
        .startDate(banner.getStartDate())
        .endDate(banner.getEndDate())
        .build();
  }
}
