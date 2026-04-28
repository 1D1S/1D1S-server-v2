package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "챌린지 생성 요청")
@Getter
@Builder
@AllArgsConstructor
public class ChallengeRequest {

  @Schema(description = "챌린지 제목", example = "30일 코딩 챌린지")
  private String title;

  @Schema(description = "챌린지 썸네일 이미지", example = "529fabd9-ae8e-4746-b82c-77725fe1a3ae")
  private String thumbnailImage;

  @Schema(description = "챌린지 카테고리", example = "DEV")
  private Category category;

  @Schema(description = "챌린지 설명", example = "매일 1시간씩 코딩 공부를 진행합니다.")
  private String description;

  @Schema(description = "챌린지 시작일", example = "2025-09-01")
  private LocalDate startDate;

  @Schema(description = "챌린지 종료일", example = "2025-09-30")
  private LocalDate endDate;

  @Schema(description = "중도 참여 허용 여부", example = "true")
  private Boolean allowMidJoin;

  @Schema(description = "챌린지 형태 (INDIVIDUAL: 개인, GROUP: 단체)", example = "GROUP")
  private ParticipationType participationType;

  @Schema(description = "최대 참여 인원", example = "10")
  private int maxParticipantCnt;

  @Schema(description = "챌린지 목표 유형 (FIXED: 고정형, FLEXIBLE: 유연형)", example = "FIXED")
  private GoalType goalType;

  @Schema(description = "챌린지 목표 목록", example = "[\"알고리즘 1문제 풀기\", \"책 10페이지 읽기\"]")
  private List<String> goals;

  @Schema(description = "챌린지 종류 (PUBLIC: 공개, PRIVATE: 비공개, OFFICIAL: 공식)", example = "PUBLIC")
  private ChallengeType challengeType;

  @Schema(description = "비공개 챌린지 비밀번호 (challengeType이 PRIVATE일 때 필수)", example = "1234")
  private String password;
}
