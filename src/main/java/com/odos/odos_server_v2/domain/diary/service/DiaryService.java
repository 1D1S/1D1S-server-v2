package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiaryService {
  public DiaryResponse createDiary(Long memberId, DiaryRequest request) {
    return null;
  }
}
