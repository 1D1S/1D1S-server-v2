package com.odos.odos_server_v2.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "presigned 업로드 정보 (파일 1개)")
@Builder
@AllArgsConstructor
@Getter
public class PresignedUploadResponse {
  @Schema(
      description = "스토리지에 직접 PUT 업로드할 presigned URL",
      example = "https://odos-bucket.s3.ap-northeast-2.amazonaws.com/uuid_test.jpeg?X-Amz-...")
  private String uploadUrl;

  @Schema(
      description = "업로드 완료 후 이미지에 접근할 최종 URL. 일지 생성/수정의 imageUrls 필드에 이 값을 넣는다.",
      example = "https://odos-bucket.s3.ap-northeast-2.amazonaws.com/uuid_test.jpeg")
  private String fileUrl;
}
