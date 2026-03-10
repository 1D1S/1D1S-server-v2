package com.odos.odos_server_v2.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "달력 스트릭 정보")
@AllArgsConstructor
@Getter
public class CalendarStreakDto {

  @Schema(description = "일지 작성 날짜", example = "2025-09-01")
  private LocalDate date;

  @Schema(description = "해당 날짜의 일지 작성 수", example = "2")
  private int count;
}
