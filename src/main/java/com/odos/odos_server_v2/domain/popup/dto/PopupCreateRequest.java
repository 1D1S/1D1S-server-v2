package com.odos.odos_server_v2.domain.popup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "홈 팝업 생성 요청(어드민)")
@Getter
@Setter
@NoArgsConstructor
public class PopupCreateRequest {

  @Schema(description = "정사각 이미지 URL(presigned 업로드 후 fileUrl). 정사각 검증은 프론트 담당.")
  private String imageUrl;

  @Schema(description = "CTA 버튼 문구", example = "자세히 보기")
  private String ctaText;

  @Schema(description = "CTA 이동 링크", example = "https://1day1streak.com/event/1")
  private String linkUrl;

  @Schema(description = "어드민 관리용 이름(선택, 클라 노출 X)", example = "여름 이벤트 팝업")
  private String title;

  @Schema(description = "게시 시작일", example = "2026-07-01")
  private LocalDate startDate;

  @Schema(description = "게시 종료일", example = "2026-07-14")
  private LocalDate endDate;
}
