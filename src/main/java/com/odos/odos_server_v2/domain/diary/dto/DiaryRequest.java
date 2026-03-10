package com.odos.odos_server_v2.domain.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
@Schema(description = "일지 생성 요청 DTO")
public class DiaryRequest {

  @Schema(description = "챌린지 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long challengeId;

  @Schema(
      description = "일지 제목",
      example = "오늘 운동 기록",
      maxLength = 100,
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Schema(
      description = "일지 내용",
      example = "하체 운동 1시간 완료",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String content;

  @Schema(
      description = "현재 감정 상태",
      example = "HAPPY",
      allowableValues = {"SAD, NORMAL, HAPPY, NONE"})
  private Feeling feeling;

  @Schema(description = "공개 여부", example = "true", defaultValue = "false")
  private Boolean isPublic;

  @Schema(description = "달성한 목표 ID 목록", example = "[1,2,3]")
  private List<Long> achievedGoalIds;

  @Schema(description = "목표 달성 날짜 (yyyy-MM-dd 형식)", example = "2026-02-24")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate achievedDate;
}
