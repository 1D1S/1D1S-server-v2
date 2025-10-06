package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.ReportRequest;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

  @PatchMapping("/{Id}")
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
  public ApiResponse<Pagination<DiaryResponse>> getAllDiaryBySize(
      @RequestParam(name = "size", required = false) Integer size,
      @RequestParam(name = "cursor", required = false) String cursor) {
    Pagination<DiaryResponse> result = diaryService.getPublicDiariesPage(size, cursor);
    return ApiResponse.success(Message.DIARY_GET_ALL_SUCCESS, result);
  }

  @GetMapping("/all")
  public ApiResponse<List<DiaryResponse>> getAllDiary() {
    List<DiaryResponse> result = diaryService.getAllPublicDiaries();
    return ApiResponse.success(Message.DIARY_GET_ALL_SUCCESS, result);
  }

  @GetMapping("/random")
  public ApiResponse<List<DiaryResponse>> randomDiary(@RequestParam("size") Long size) {
    List<DiaryResponse> result = diaryService.getRandomDiaries(size);
    return ApiResponse.success(Message.DIARY_VIEW_RANDOM, result);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> deleteDiary(@PathVariable(name = "id") Long diaryId) {
    Boolean result = diaryService.deleteDiary(diaryId);
    return ApiResponse.success(Message.DIARY_DELETE_SUCCESS, result);
  }

  @PostMapping("/{id}/likes")
  public ApiResponse<Integer> addLike(@PathVariable(name = "id") Long diaryId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Integer likeCount = diaryService.addDiaryLike(memberId, diaryId);
    return ApiResponse.success(Message.DIARY_ADDED_LIKE, likeCount);
  }

  @DeleteMapping("/{id}/likes")
  public ApiResponse<Integer> cancelLike(@PathVariable(name = "id") Long diaryId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Integer likeCount = diaryService.cancelDiaryLike(memberId, diaryId);
    return ApiResponse.success(Message.DIARY_CANCELED_LIKE, likeCount);
  }

  @PostMapping("/report")
  public ApiResponse<Boolean> reportDiary(@RequestBody ReportRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Boolean result = diaryService.reportDiary(request, memberId);
    return ApiResponse.success(Message.DIARY_REPORT_CREATED, result);
  }

  @GetMapping("/my")
  public ApiResponse<List<DiaryResponse>> getMyDiaries() {
    List<DiaryResponse> result = diaryService.getMyDiaries();
    return ApiResponse.success(Message.DIARY_GET_MY_ALL_SUCCESS, result);
  }

  @PostMapping("/{id}/image")
  public ApiResponse<String> uploadImage(
      @PathVariable("id") Long diaryId, @RequestParam("file") MultipartFile file) {
    String fileName = diaryService.uploadDiaryFile(diaryId, file);
    return ApiResponse.success(Message.DIARY_IMAGE_UPLOADED, fileName);
  }

  @PostMapping("/{id}/images")
  public ApiResponse<List<String>> uploadImages(
      @PathVariable("id") Long diaryId, @RequestParam("file") List<MultipartFile> files) {
    List<String> fileList = diaryService.uploadDiaryFiles(diaryId, files);
    return ApiResponse.success(Message.DIARY_IMAGES_UPLOADED, fileList);
  }
}
