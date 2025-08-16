package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
@Slf4j
public class DiaryController {
  private final DiaryService diaryService;

  @PostMapping("")
  public ApiResponse<DiaryResponse> createDiary(@RequestBody DiaryRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    DiaryResponse result = diaryService.createDiary(memberId, request);
    return ApiResponse.success(Message.DIARY_CREATE_SUCCESS, result);
  }
}
