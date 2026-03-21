package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(
    description = "챌린지 생성 요청",
    example =
        """
        {
          "title": "30일 코딩 챌린지",
          "category": "DEV",
          "description": "매일 1시간씩 코딩 공부를 진행합니다.",
          "startDate": "2025-09-01",
          "endDate": "2025-09-30",
          "maxParticipantCnt": 10,
          "challengeType": "FIXED",
          "goals": ["알고리즘 1문제 풀기", "책 10페이지 읽기"]
        }
        """)
@Getter
@Builder
@AllArgsConstructor
public class ChallengeRequest {

  @Schema(description = "챌린지 제목", example = "30일 코딩 챌린지")
  private String title;

  @Schema(description = "챌린지 카테고리", example = "DEV")
  private Category category;

  @Schema(description = "챌린지 설명", example = "매일 1시간씩 코딩 공부를 진행합니다.")
  private String description;

  @Schema(description = "챌린지 시작일", example = "2025-09-01")
  private LocalDate startDate;

  @Schema(description = "챌린지 종료일", example = "2025-09-30")
  private LocalDate endDate;

  @Schema(description = "최대 참여 인원", example = "10")
  private int maxParticipantCnt;

  @Schema(description = "챌린지 유형 (FIXED: 고정형, FLEXIBLE: 유연형)", example = "FIXED")
  private ChallengeType challengeType;

  @Schema(description = "챌린지 목표 목록", example = "[\"알고리즘 1문제 풀기\", \"책 10페이지 읽기\"]")
  private List<String> goals;
}
