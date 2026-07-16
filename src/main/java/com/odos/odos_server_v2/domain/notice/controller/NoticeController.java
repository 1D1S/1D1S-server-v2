package com.odos.odos_server_v2.domain.notice.controller;

import com.odos.odos_server_v2.domain.notice.dto.NoticeResponse;
import com.odos.odos_server_v2.domain.notice.service.NoticeService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notice API", description = "공지 조회 API")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

  private final NoticeService noticeService;

  @Operation(
      summary = "공지 목록",
      description = "고정 먼저(고정끼리 등록순) → 비고정 최신순. page(기본 0)/size(기본 20) 페이지네이션.")
  @GetMapping
  public ApiResponse<OffsetPagination<NoticeResponse>> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ApiResponse.success(Message.GET_NOTICE_LIST, noticeService.getNotices(pageable));
  }

  @Operation(summary = "공지 단건 조회")
  @GetMapping("/{id}")
  public ApiResponse<NoticeResponse> getOne(@PathVariable Long id) {
    return ApiResponse.success(Message.GET_NOTICE, noticeService.getNotice(id));
  }
}
