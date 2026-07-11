package com.odos.odos_server_v2.domain.popup.controller;

import com.odos.odos_server_v2.domain.popup.dto.PopupAdminResponse;
import com.odos.odos_server_v2.domain.popup.dto.PopupCreateRequest;
import com.odos.odos_server_v2.domain.popup.dto.PopupUpdateRequest;
import com.odos.odos_server_v2.domain.popup.service.PopupService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Popup API", description = "관리자 홈 팝업 API (ADMIN 권한 필요)")
@RestController
@RequestMapping("/admin/popups")
@RequiredArgsConstructor
public class AdminPopupController {

  private final PopupService popupService;

  @Operation(
      summary = "팝업 생성",
      description =
          "정사각 이미지는 POST /image/presigned-urls 로 업로드 후 응답 fileUrl 을 imageUrl 로 전달. 정사각 검증은 프론트 담당.")
  @PostMapping
  public ApiResponse<PopupAdminResponse> create(@RequestBody PopupCreateRequest request) {
    return ApiResponse.success(Message.CREATE_POPUP, popupService.create(request));
  }

  @Operation(summary = "팝업 수정", description = "포함한 필드만 변경, 생략 필드는 유지. isActive 로 노출 on/off.")
  @PatchMapping("/{id}")
  public ApiResponse<PopupAdminResponse> update(
      @PathVariable Long id, @RequestBody PopupUpdateRequest request) {
    return ApiResponse.success(Message.UPDATE_POPUP, popupService.update(id, request));
  }

  @Operation(summary = "팝업 삭제")
  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    popupService.delete(id);
    return ApiResponse.success(Message.DELETE_POPUP);
  }

  @Operation(
      summary = "팝업 목록(달력 뷰)",
      description = "from/to(선택)와 게시기간이 겹치는 팝업. 둘 다 미지정 시 전체. 시작일 오름차순. 게시 기간·이미지·제목·활성 포함.")
  @GetMapping
  public ApiResponse<List<PopupAdminResponse>> list(
      @Parameter(description = "조회 범위 시작(포함). 미지정 시 하한 없음", example = "2026-07-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @Parameter(description = "조회 범위 종료(포함). 미지정 시 상한 없음", example = "2026-07-31")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to) {
    return ApiResponse.success(Message.GET_POPUP_LIST, popupService.getForAdmin(from, to));
  }

  @Operation(summary = "팝업 단건 조회")
  @GetMapping("/{id}")
  public ApiResponse<PopupAdminResponse> getOne(@PathVariable Long id) {
    return ApiResponse.success(Message.GET_POPUP, popupService.getOne(id));
  }
}
