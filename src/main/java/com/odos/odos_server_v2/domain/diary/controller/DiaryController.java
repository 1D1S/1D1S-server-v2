package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import java.util.List;
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

  @PutMapping("/{Id}")
  public ApiResponse<DiaryResponse> updateDiary(
      @PathVariable(name = "Id") Long diaryId, @RequestBody DiaryRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    DiaryResponse result = diaryService.updateDiary(memberId, diaryId, request);
    return ApiResponse.success(Message.DIARY_UPDATE_SUCCESS, result);
  }

  @GetMapping("/{Id}")
  public ApiResponse<DiaryResponse> getDiary(@PathVariable(name = "Id") Long diaryId) {
    DiaryResponse result = diaryService.getDiary(diaryId);
    return ApiResponse.success(Message.DIARY_GET_SUCCESS, result);
  }

  @GetMapping("")
  public ApiResponse<List<DiaryResponse>> getAllDiary() {
    List<DiaryResponse> result = diaryService.getAllPublicDiaries();
    return ApiResponse.success(Message.DIARY_GET_ALL_SUCCESS, result);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> deleteDiary(@PathVariable(name = "id") Long diaryId) {
    Boolean result = diaryService.deleteDiary(diaryId);
    return ApiResponse.success(Message.DIARY_DELETE_SUCCESS, result);
  }
}
