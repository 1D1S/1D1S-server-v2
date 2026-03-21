package com.odos.odos_server_v2.domain.image.controller;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.image.dto.ImageUploadRequest;
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
          presigned url을 발급한다. 현재는 프로필 이미지 저장 및 수정에 사용한다.

          [프로필 이미지 저장]
          1. /image/presigned-url API 호출
          2. 응답의 presignedUrl 필드 값에 이미지 저장
          3. 응답의 objectKey 값을 /signup/info API의 profileImageKey 필드에 넣어 호출
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
}
