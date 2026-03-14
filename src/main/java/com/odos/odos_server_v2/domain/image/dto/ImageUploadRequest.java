package com.odos.odos_server_v2.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "업로드 할 이미지 정보")
@Builder
@AllArgsConstructor
@Getter
public class ImageUploadRequest {
  @Schema(description = "이미지 파일 이름", example = "testImage.jpeg")
  private String fileName;

  @Schema(description = "이미지 파일 타입", example = "image/jpeg")
  private String fileType;
}
