package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
@Slf4j
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("")
    public ResponseEntity<DiaryResponse> createDiary(
            /*@PathVariable(name = "memberId") Long memberId, */@RequestBody DiaryRequest request) {
        try {
            return ResponseEntity.ok(diaryService.createDiary(/*memberId, */request));
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.ok(diaryService.createDiary(/*memberId, */request));
        }
    }

}
