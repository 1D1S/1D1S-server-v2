package com.odos.odos_server_v2.domain.story.dto;

import java.time.LocalDateTime;

public interface StoryDiarySummaryProjection {

  Long getDiaryId();

  String getDiaryTitle();

  LocalDateTime getCreatedAt();

  Long getMemberId();

  String getMemberNickname();

  String getMemberProfileUrl();
}
