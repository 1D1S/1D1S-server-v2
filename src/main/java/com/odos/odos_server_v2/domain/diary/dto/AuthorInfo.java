package com.odos.odos_server_v2.domain.diary.dto;

import lombok.Builder;

@Builder
public class AuthorInfo {
  private Long id;
  private String nickname;
  private String profileImage;
}
