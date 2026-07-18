package com.odos.odos_server_v2.domain.vote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "투표 제출 요청")
public class VoteSubmitRequest {
  @NotNull
  @Schema(description = "선택한 항목 ID. 단일 선택 투표는 정확히 1개", example = "[1]")
  private List<Long> optionIds;
}
