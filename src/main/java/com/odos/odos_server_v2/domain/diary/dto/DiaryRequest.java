package com.odos.odos_server_v2.domain.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class DiaryRequest {
  private Long challengeId;
  private String title;
  private String content;
  private Feeling feeling;
  private Boolean isPublic;
  private List<Long> achievedGoalIds;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate achievedDate;
}
