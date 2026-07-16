package com.odos.odos_server_v2.domain.banner.dto;

import com.odos.odos_server_v2.domain.banner.entity.Banner;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "배너 응답")
public record BannerResponse(
    @Schema(description = "배너 ID", example = "1") Long id,
    @Schema(description = "배너 제목", example = "여름 이벤트") String title,
    @Schema(description = "배너 부제목", example = "7월 한정 혜택") String subtitle,
    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/banner.png")
        String imageUrl,
    @Schema(description = "배너 클릭 이동 링크", example = "https://1day1streak.com/event/1")
        String linkUrl,
    @Schema(description = "게시 시작일", example = "2026-07-01") LocalDate startDate,
    @Schema(description = "게시 종료일", example = "2026-07-09") LocalDate endDate) {

  public static BannerResponse from(Banner banner) {
    return new BannerResponse(
        banner.getId(),
        banner.getTitle(),
        banner.getSubtitle(),
        banner.getImageUrl(),
        banner.getLinkUrl(),
        banner.getStartDate(),
        banner.getEndDate());
  }
}
