package com.odos.odos_server_v2.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "닉네임 변경 요청")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class NicknameRequest {
  @Schema(description = "닉네임 (한글 또는 영어, 8자 이내, 특수문자 불가)", example = "내손안의흙염룡")
  private String nickname;
}
