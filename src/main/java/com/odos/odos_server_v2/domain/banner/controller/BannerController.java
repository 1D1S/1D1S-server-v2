package com.odos.odos_server_v2.domain.banner.controller;

import com.odos.odos_server_v2.domain.banner.dto.BannerResponse;
import com.odos.odos_server_v2.domain.banner.service.BannerService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Banner API", description = "사용자용 배너 API (인증 불필요, 게스트 포함)")
@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

  private final BannerService bannerService;

  @Operation(
      summary = "오늘의 배너 목록 조회 (공개)",
      description =
          "오늘 날짜(KST)가 게시 기간에 포함된 배너 목록을 조회합니다. 인증 없이 게스트도 접근할 수 있으며 "
              + "조회 조건은 startDate <= today <= endDate, 정렬은 시작일 오름차순·ID 오름차순입니다.")
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
                            """)))
  })
  @GetMapping
  public ApiResponse<List<BannerResponse>> getBanners() {
    return ApiResponse.success(Message.GET_TODAY_BANNERS, bannerService.getActiveBanners());
  }
}
