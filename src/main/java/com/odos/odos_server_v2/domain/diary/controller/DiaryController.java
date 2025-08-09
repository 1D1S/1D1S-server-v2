package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
public class DiaryController {
  private final DiaryService diaryService;

  @PostMapping("/{memberId}")
  public Response createDiary(
      @PathVariable(name = "memberId") Long memberId, @RequestBody DiaryRequest request) {
    DiaryResponse result = diaryService.createDiary(memberId, request);
    return Response.success("", result);
  }
}
