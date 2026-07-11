package com.odos.odos_server_v2.domain.popup.dto;

import com.odos.odos_server_v2.domain.popup.entity.Popup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 어드민용 팝업 응답(달력 뷰·상세). 게시 기간/활성/관리명 포함. */
@Schema(description = "홈 팝업 응답(어드민)")
public record PopupAdminResponse(
    Long id,
    String popupKey,
    String imageUrl,
    String ctaText,
    String linkUrl,
    String title,
    LocalDate startDate,
    LocalDate endDate,
    boolean isActive,
    LocalDateTime createdAt) {

  public static PopupAdminResponse from(Popup p) {
    return new PopupAdminResponse(
        p.getId(),
        p.getPopupKey(),
        p.getImageUrl(),
        p.getCtaText(),
        p.getLinkUrl(),
        p.getTitle(),
        p.getStartDate(),
        p.getEndDate(),
        p.isActive(),
        p.getCreatedAt());
  }
}
