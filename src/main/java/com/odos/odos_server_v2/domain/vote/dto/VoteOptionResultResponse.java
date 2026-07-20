package com.odos.odos_server_v2.domain.vote.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoteOptionResultResponse {
  private Long optionId;
  private String text;

  @Schema(description = "전체 응답자 중 해당 항목을 선택한 비율. 관리자 설문은 사용자 응답에서 제외", example = "42.86")
  private BigDecimal percentage;
}
