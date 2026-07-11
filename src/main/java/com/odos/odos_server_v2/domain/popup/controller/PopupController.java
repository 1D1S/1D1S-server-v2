package com.odos.odos_server_v2.domain.popup.controller;

import com.odos.odos_server_v2.domain.popup.dto.PopupResponse;
import com.odos.odos_server_v2.domain.popup.service.PopupService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Popup API", description = "홈 팝업 조회 API")
@RestController
@RequestMapping("/popups")
@RequiredArgsConstructor
public class PopupController {

  private final PopupService popupService;

  @Operation(
      summary = "오늘 게시 중 팝업 목록",
      description = "오늘(KST) 게시 중인 활성 팝업을 시작일 오름차순으로 반환. 클라는 popupKey 로 '다시보지않기' 쿠키를 대조해 노출한다.")
  @GetMapping("/active")
  public ApiResponse<List<PopupResponse>> getActivePopups() {
    return ApiResponse.success(Message.GET_ACTIVE_POPUPS, popupService.getActivePopups());
  }
}
