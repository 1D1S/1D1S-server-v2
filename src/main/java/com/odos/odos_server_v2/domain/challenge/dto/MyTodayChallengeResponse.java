package com.odos.odos_server_v2.domain.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "홈 '오늘의 기록'용 진행 중 챌린지 요약(목표 + 오늘 일지 작성 여부)")
@Builder
@AllArgsConstructor
@Getter
public class MyTodayChallengeResponse {

  @Schema(description = "챌린지 ID", example = "1")
  private Long challengeId;

  @Schema(description = "챌린지 제목", example = "30일 코딩 챌린지")
  private String title;

  @Schema(description = "오늘(KST) 해당 챌린지에 일지를 작성했는지 여부(completedDate 기준, 삭제 제외)", example = "false")
  private boolean todayWritten;

  @Schema(description = "내 챌린지 목표 목록")
  private List<ChallengeGoalDto> goals;
}
