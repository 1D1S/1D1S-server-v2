package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryCoreInfoDto {
  private Long id;
  private Long challengeId;
  private String title;
  private long likeCnt;
  private String imgUrl;
  private Boolean isPublic;
  private String createdAt;
  private String challengedDate;
  private Feeling feeling;
  private int achievement;
}
