package com.odos.odos_server_v2.domain.member.dto;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "사이드바 응답")
@Builder
@Getter
public class SideBarDto {

  @Schema(description = "닉네임", example = "홍길동")
  private String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileUrl;

  @Schema(description = "현재 연속 일지 작성 일수 (스트릭)", example = "5")
  private int streakCount;

  @Schema(description = "오늘 달성해야 할 목표 수 (현재 참여 중인 챌린지 기준)", example = "3")
  private int todayGoalCount;

  @Schema(description = "참여 중인 챌린지 목록")
  List<ChallengeSummaryResponse> challengeList;
}
