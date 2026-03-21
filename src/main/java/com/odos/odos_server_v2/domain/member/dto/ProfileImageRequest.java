package com.odos.odos_server_v2.domain.member.dto;

import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 변경 요청")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ProfileImageRequest {
  @Schema(description = "profile image object key", example = "f91832a-26c3-43ff-b2b8-0734bffa4295")
  private String objectKey;
}
