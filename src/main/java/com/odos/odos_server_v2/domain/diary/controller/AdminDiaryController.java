package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryDeleteAdminRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryDeleteAdminResponse;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.service.AdminDiaryService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Slf4j
@Tag(name = "Admin Diary API", description = "관리자 일지 API")
public class AdminDiaryController {
  private final AdminDiaryService diaryService;

  @Operation(summary = "관리자 일지 목록 조회", description = "작성자, 챌린지, 최신순/좋아요순 필터를 적용해 일지를 조회합니다.")
  @GetMapping("")
  public ApiResponse<OffsetPagination<DiaryResponse>> getAllDiaryByOffset(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(name = "filterType", defaultValue = "latest") String filterType,
      @RequestParam(name = "authorNickname", required = false) String authorNickname,
      @RequestParam(name = "challengeId", required = false) Long challengeId) {
    Pageable pageable = PageRequest.of(page, size);
    OffsetPagination<DiaryResponse> result =
        diaryService.getDiariesByAdmin(pageable, filterType, authorNickname, challengeId);
    return ApiResponse.success(Message.DIARY_GET_ALL_SUCCESS, result);
  }

  @Operation(summary = "관리자 일지 상세 조회", description = "일지 ID를 기반으로 일지 상세 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ApiResponse<DiaryResponse> getDiary(@PathVariable(name = "id") Long diaryId) {
    DiaryResponse result = diaryService.getDiaryByAdmin(diaryId);
    return ApiResponse.success(Message.DIARY_GET_SUCCESS, result);
  }

  @Operation(summary = "관리자 챌린지별 일지 조회", description = "챌린지 ID를 기반으로 일지 목록을 조회합니다.")
  @GetMapping("/challenges/{challengeId}")
  public ApiResponse<OffsetPagination<DiaryResponse>> getChallengeDiaries(
      @PathVariable(name = "challengeId") Long challengeId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    OffsetPagination<DiaryResponse> diaries =
        diaryService.getChallengeDiariesByAdmin(challengeId, pageable);
    return ApiResponse.success(Message.CHALLENGE_DIARY_GET, diaries);
  }

  @Operation(summary = "관리자 일지 삭제", description = "부적절한 일지를 삭제 사유와 함께 삭제 처리합니다.")
  @PatchMapping("")
  public ApiResponse<DiaryDeleteAdminResponse> deleteDiary(
      @RequestBody DiaryDeleteAdminRequest request) {
    DiaryDeleteAdminResponse result = diaryService.deleteDiaryByAdmin(request);
    return ApiResponse.success(Message.DIARY_DELETE_SUCCESS, result);
  }
}
