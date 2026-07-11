package com.odos.odos_server_v2.domain.popup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "홈 팝업 수정 요청(어드민). 포함한 필드만 변경, 생략 필드는 유지.")
@Getter
@Setter
@NoArgsConstructor
public class PopupUpdateRequest {
  private String imageUrl;
  private String ctaText;
  private String linkUrl;
  private String title;
  private LocalDate startDate;
  private LocalDate endDate;

  @Schema(description = "노출 on/off", example = "true")
  private Boolean isActive;
}
