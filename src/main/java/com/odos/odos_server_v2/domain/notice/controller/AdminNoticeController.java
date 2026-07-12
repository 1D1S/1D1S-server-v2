package com.odos.odos_server_v2.domain.notice.controller;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.notice.dto.NoticeCreateRequest;
import com.odos.odos_server_v2.domain.notice.dto.NoticeResponse;
import com.odos.odos_server_v2.domain.notice.dto.NoticeUpdateRequest;
import com.odos.odos_server_v2.domain.notice.service.NoticeService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Notice API", description = "관리자 공지 API (ADMIN 권한 필요)")
@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

  private final NoticeService noticeService;

  @Operation(summary = "공지 생성", description = "pinned=true 면 목록 최상단 고정. 작성 관리자는 감사용으로 저장됨.")
  @PostMapping
  public ApiResponse<NoticeResponse> create(@RequestBody NoticeCreateRequest request) {
    Long adminId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(Message.CREATE_NOTICE, noticeService.create(request, adminId));
  }

  @Operation(summary = "공지 수정", description = "포함한 필드만 변경, 생략 필드는 유지.")
  @PatchMapping("/{id}")
  public ApiResponse<NoticeResponse> update(
      @PathVariable Long id, @RequestBody NoticeUpdateRequest request) {
    return ApiResponse.success(Message.UPDATE_NOTICE, noticeService.update(id, request));
  }

  @Operation(summary = "공지 삭제")
  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    noticeService.delete(id);
    return ApiResponse.success(Message.DELETE_NOTICE);
  }

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
