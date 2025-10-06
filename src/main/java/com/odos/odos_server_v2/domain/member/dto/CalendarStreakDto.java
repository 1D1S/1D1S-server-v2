package com.odos.odos_server_v2.domain.member.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CalendarStreakDto {
  private LocalDate date;
  private int count;
}
