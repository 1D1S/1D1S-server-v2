package com.odos.odos_server_v2.domain.popup.dto;

import com.odos.odos_server_v2.domain.popup.entity.Popup;
import io.swagger.v3.oas.annotations.media.Schema;

/** 클라(홈)용 팝업 응답. 다시보지않기 대조는 popupKey 로. */
@Schema(description = "홈 팝업 응답(클라)")
public record PopupResponse(String popupKey, String imageUrl, String ctaText, String linkUrl) {

  public static PopupResponse from(Popup p) {
    return new PopupResponse(p.getPopupKey(), p.getImageUrl(), p.getCtaText(), p.getLinkUrl());
  }
}
