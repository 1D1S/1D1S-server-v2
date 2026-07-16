package com.odos.odos_server_v2.domain.banner.controller;

import com.odos.odos_server_v2.domain.banner.dto.BannerCreateRequest;
import com.odos.odos_server_v2.domain.banner.dto.BannerResponse;
import com.odos.odos_server_v2.domain.banner.service.BannerService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Banner API", description = "관리자 배너 API (ADMIN 권한 필요)")
@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

  private final BannerService bannerService;

  @Operation(
      summary = "관리자 배너 전체 목록 조회",
      description = "관리자가 등록된 모든 배너를 게시 기간과 무관하게 조회합니다. 시작일 오름차순으로 정렬됩니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "배너 전체 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "배너 목록 조회 성공했습니다.",
                              "data": [
                                {
                                  "id": 1,
                                  "title": "여름 이벤트",
                                  "subtitle": "7월 한정 혜택",
                                  "imageUrl": "https://cdn.example.com/banner.png",
                                  "linkUrl": "https://1day1streak.com/event/1",
                                  "startDate": "2026-07-01",
                                  "endDate": "2026-07-09"
                                },
                                {
                                  "id": 2,
                                  "title": "가을 이벤트",
                                  "subtitle": "9월 챌린지 혜택",
                                  "imageUrl": "https://cdn.example.com/banner-fall.png",
                                  "linkUrl": "https://1day1streak.com/event/2",
                                  "startDate": "2026-09-01",
                                  "endDate": "2026-09-30"
                                }
                              ]
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "AUTH-001",
                              "message": "인증되지 않은 접근입니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자가 아닌 회원",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "USER-009",
                              "message": "관리자 회원만 요청이 가능합니다."
                            }
                            """)))
  })
  @GetMapping
  public ApiResponse<List<BannerResponse>> getAllBanners() {
    return ApiResponse.success(Message.GET_BANNER_LIST, bannerService.getAllBanners());
  }

  @Operation(
      summary = "오늘의 배너 목록 조회",
      description =
          "관리자가 오늘 날짜(KST)가 게시 기간에 포함된 배너 목록을 조회합니다. "
              + "조회 조건은 startDate <= today <= endDate 입니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "오늘의 배너 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "오늘의 배너 목록 조회 성공했습니다.",
                              "data": [
                                {
                                  "id": 1,
                                  "title": "여름 이벤트",
                                  "subtitle": "7월 한정 혜택",
                                  "imageUrl": "https://cdn.example.com/banner.png",
                                  "linkUrl": "https://1day1streak.com/event/1",
                                  "startDate": "2026-07-01",
                                  "endDate": "2026-07-09"
                                }
                              ]
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "AUTH-001",
                              "message": "인증되지 않은 접근입니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자가 아닌 회원",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "USER-009",
                              "message": "관리자 회원만 요청이 가능합니다."
                            }
                            """)))
  })
  @GetMapping("/today")
  public ApiResponse<List<BannerResponse>> getTodayBanners() {
    return ApiResponse.success(Message.GET_TODAY_BANNERS, bannerService.getTodayBanners());
  }

  @Operation(
      summary = "배너 생성",
      description =
          "관리자가 제목, 부제목, 이미지 URL, 링크, 게시 기간을 입력해 배너를 생성합니다. "
              + "이미지는 presigned 업로드 후 받은 fileUrl을 imageUrl로 전달합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "배너 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "배너 생성 성공했습니다.",
                              "data": {
                                "id": 1,
                                "title": "여름 이벤트",
                                "subtitle": "7월 한정 혜택",
                                "imageUrl": "https://cdn.example.com/banner.png",
                                "linkUrl": "https://1day1streak.com/event/1",
                                "startDate": "2026-07-01",
                                "endDate": "2026-07-09"
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "필수값 누락",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "required field missing",
                        value =
                            """
                            {
                              "code": "BANNER-002",
                              "message": "배너 필수 항목(title/subtitle/imageUrl/linkUrl/기간)이 누락되었습니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "기간 오류",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "invalid period",
                        value =
                            """
                            {
                              "code": "BANNER-001",
                              "message": "배너 게시 기간이 올바르지 않습니다. (시작일 <= 종료일)"
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "AUTH-001",
                              "message": "인증되지 않은 접근입니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자가 아닌 회원",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "USER-009",
                              "message": "관리자 회원만 요청이 가능합니다."
                            }
                            """)))
  })
  @PostMapping
  public ApiResponse<BannerResponse> create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "관리자 배너 생성 요청",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = BannerCreateRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                  {
                                    "title": "여름 이벤트",
                                    "subtitle": "7월 한정 혜택",
                                    "imageUrl": "https://cdn.example.com/banner.png",
                                    "linkUrl": "https://1day1streak.com/event/1",
                                    "startDate": "2026-07-01",
                                    "endDate": "2026-07-09"
                                  }
                                  """)))
          @RequestBody
          BannerCreateRequest request) {
    return ApiResponse.success(Message.CREATE_BANNER, bannerService.create(request));
  }

  @Operation(summary = "배너 삭제", description = "관리자가 배너를 삭제합니다. 삭제된 배너는 복구되지 않습니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "배너 삭제 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "배너 삭제 성공했습니다.",
                              "data": null
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "AUTH-001",
                              "message": "인증되지 않은 접근입니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "관리자가 아닌 회원",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "USER-009",
                              "message": "관리자 회원만 요청이 가능합니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 배너",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "code": "BANNER-003",
                              "message": "존재하지 않는 배너입니다."
                            }
                            """)))
  })
  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    bannerService.delete(id);
    return ApiResponse.success(Message.DELETE_BANNER);
  }
}
