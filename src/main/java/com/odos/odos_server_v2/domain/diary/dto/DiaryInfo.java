package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DiaryInfo {
  private String createdAt;
  private String challengedDate;
  private Feeling feeling;
  private List<Long> achievement;
  private int achievementRate;
}
