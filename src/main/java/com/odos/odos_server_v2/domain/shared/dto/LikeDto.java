package com.odos.odos_server_v2.domain.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeDto {
  private boolean likedByMe;
  private long likeCnt;
}
