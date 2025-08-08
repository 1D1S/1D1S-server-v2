package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
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
  private String achievedDate;
}
