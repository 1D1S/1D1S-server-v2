package com.odos.odos_server_v2.domain.member.dto;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "마이페이지 응답")
@Builder
@Getter
public class MyPageDto {

  @Schema(description = "닉네임", example = "홍길동")
  String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  String profileUrl;

  @Schema(description = "스트릭 통계 정보")
  StreakDto streak;

  @Schema(description = "참여 중인 챌린지 목록")
  List<ChallengeSummaryResponse> challengeList;

  @Schema(description = "내가 작성한 일지 목록")
  List<DiaryResponse> diaryList;
}
