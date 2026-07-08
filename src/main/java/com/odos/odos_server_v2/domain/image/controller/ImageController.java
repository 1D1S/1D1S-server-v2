package com.odos.odos_server_v2.domain.image.controller;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.image.dto.ImageUploadRequest;
import com.odos.odos_server_v2.domain.image.dto.PresignedUploadResponse;
import com.odos.odos_server_v2.domain.image.dto.PresignedUrlResponse;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이미지", description = "이미지 API")
@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {
  private final ImageService imageService;

  @Operation(
      summary = "presigned url 발급",
      description =
          """
          presigned url을 발급한다. 프로필 이미지 저장 및 수정, 챌린지 썸네일 이미지 저장 및 수정에 사용한다.

          1. /image/presigned-url API 호출
          2. 응답의 presignedUrl 필드 값에 이미지 저장

          [프로필 이미지 저장]
          3. 응답의 objectKey 값을 PUT /signup/info API의 profileImageKey 필드에 넣어 호출

          [프로필 이미지 수정]
          3. 응답의 objectKey 값을 PATCH /member/profile-image API의 objectKey 필드에 넣어 호출

          [챌린지 썸네일 이미지 저장]
          3. 응답의 objectKey 값을 POST /challenges API의 thumbnailImage 필드에 넣어 호출

          [챌린지 썸네일 이미지 수정]
          3. 응답의 objectKey 값을 PATCH /challenges/{challengeId} API의 thumbnailImage 필드에 넣어 호출
          """)
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "presigned url 발급 성공했습니다.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChallengeSummaryResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                         {
                                             "message": "presigned url 발급 성공했습니다.",
                                            "data": {
                                              "presignedUrl": "https://odos-bucket.s3.ap-northeast-2.amazonaws.com/52-Amz-S135e4...",
                                              "objectKey": "529fabd9-ae8e-4746-b82c-77725fe1a3ae"
                                            }
                                          }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                          { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                          { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                                          """)))
  })
  @PostMapping("/presigned-url")
  public ApiResponse<PresignedUrlResponse> generatePresignedUrl(
      @RequestBody ImageUploadRequest imageUploadRequest) {
    return ApiResponse.success(
        Message.CREATE_PRESIGNED_URL_SUCCESS,
        imageService.createPresignedUrl(
            imageUploadRequest.getFileName(), imageUploadRequest.getFileType()));
  }

  @Operation(
      summary = "presigned url 여러 개 발급 (일지 이미지용)",
      description =
          """
          일지 이미지 여러 장을 위한 presigned url을 한 번에 발급한다.

          1. 업로드할 파일 목록(fileName, fileType)을 배열로 보내 이 API 호출
          2. 응답 각 항목의 uploadUrl 로 파일을 직접 PUT 업로드
             - PUT 요청 헤더에 반드시 `Content-Type: <요청한 fileType>` 을 포함해야 한다.
          3. 응답 각 항목의 fileUrl 배열을 모아 일지 생성/수정 API(POST/PATCH /diaries)의 imageUrls 필드에 넣어 호출
          """)
  @PostMapping("/presigned-urls")
  public ApiResponse<List<PresignedUploadResponse>> generatePresignedUploadUrls(
      @RequestBody List<ImageUploadRequest> imageUploadRequests) {
    return ApiResponse.success(
        Message.CREATE_PRESIGNED_URL_SUCCESS,
        imageService.createPresignedUploadUrls(imageUploadRequests));
  }
}
