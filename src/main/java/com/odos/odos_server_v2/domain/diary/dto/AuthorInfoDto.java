package com.odos.odos_server_v2.domain.diary.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthorInfoDto {
  private Long id;
  private String nickname;
  private String profileImage;
}
