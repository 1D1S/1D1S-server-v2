package com.odos.odos_server_v2.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "presigned url 정보")
@Builder
@AllArgsConstructor
@Getter
public class PresignedUrlResponse {
  @Schema(
      description = "발급된 presigned url",
      example = "https://bucket-name.s3.ap-northeast-2.amazonaws.com...")
  private String presignedUrl;

  @Schema(description = "이미지 접근 key", example = "9f91832a-26c3-43ff-b2b8-0734bffa4295.jpeg")
  private String objectKey;
}
